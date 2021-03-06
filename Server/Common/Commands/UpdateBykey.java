package Common.Commands;

import Common.*;
import Utility.DBworking;
import Utility.ServerReceiver;
import Utility.ServerSender;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

/**
 * The type Update bykey.
 */
public class UpdateBykey implements Command {
    /**
     * Instantiates a new Update bykey.
     */
    public UpdateBykey(){
        Invoker.regist("update_key",this);
    }
    @Override
    public void execute(String par1, Socket clientSocket,String user) throws SQLException {
        DBworking dBworking = new DBworking();
        dBworking.ConnectionToDB();
        ServerSender serverSender = new ServerSender();
        ServerReceiver serverReceiver = new ServerReceiver();
        try {
            if (ExecuteScript.inExecution && (par1 == null || par1.equals(""))) {
                serverSender.send(clientSocket,"Параметр не был указан,выполнение команды \"update_key\" невозможно.", 2);
            } else if (ExecuteScript.inExecution) {
                Long key = Long.parseLong(par1);
                dBworking.loadAllTickets();
                TicketCollection.getLock().writeLock().lock();
                TicketCollection ticketCollection = new TicketCollection();
                TreeMap<Long, Ticket> tickets = ticketCollection.getTickets();
                boolean keyExist = false;
                String ticketUser = "";
                Long ticketid = 0L;
                if (tickets.size() > 0) {
                    for (Map.Entry<Long, Ticket> entry : tickets.entrySet()) {
                        if (entry.getKey() == key) {
                            keyExist = true;
                            ticketUser = entry.getValue().getUser();
                            ticketid = Long.valueOf(entry.getValue().getPerson().getPassportID());
                            break;
                        }
                    }
                    TicketCollection.getLock().writeLock().unlock();
                } else if (ExecuteScript.inExecution) serverSender.send(clientSocket,"Коллекция пуста", 2);
                else serverSender.send(clientSocket,"Коллекция пуста", 0);
                if (keyExist&&ticketUser.equals(user)) {
                    Map.Entry entry = (Decoder.decodeIntoCollection(ExecuteScript.getExecuteData()).firstEntry());
                    Ticket ticket = (Ticket) entry.getValue();
                    ticket.setMapKey(key);
                    ticket.setUser(ticketUser);
                    ticket.getPerson().setPassportID(ticketid);
                    ticket.setCreationDate(Timestamp.from(Instant.now()));
                    ticketCollection.replaceMovie(key, ticket);
                    serverSender.send(clientSocket,"Элемент с ключом " + key + " обновлён.", 2);
                    dBworking.uploadAllTickets();
                } else {
                    serverSender.send(clientSocket,"Элемент с заданным ключом не существует или вам не принадлежит,попробуйте ввести команду снова.", 2);
                }
            } else if (par1 == null) {
                String key;
                serverSender.send(clientSocket,"Укажите ключ для нового элемента.", 1);
                key = (String) serverReceiver.receive(clientSocket);
                if (key.equals("") || key == null) {
                    serverSender.send(clientSocket,"Ключ не может быть null", 2);
                    this.execute(par1, clientSocket, user);
                } else this.execute(key, clientSocket,user);
            } else {
                dBworking.loadAllTickets();
                TicketCollection.getLock().writeLock().lock();
                Long key = Long.parseLong(par1);
                TicketCollection ticketCollection = new TicketCollection();
                TreeMap<Long, Ticket> tickets = ticketCollection.getTickets();
                TicketCollection.getLock().writeLock().unlock();
                boolean keyExist = false;
                String ticketUser = "";
                Long ticketid = 0L;
                if (tickets.size() > 0) {
                    for (Map.Entry<Long, Ticket> entry : tickets.entrySet()) {
                        if (entry.getKey().equals(key)) {
                            keyExist = true;
                            ticketUser = entry.getValue().getUser();
                            ticketid = Long.valueOf(entry.getValue().getPerson().getPassportID());
                            break;
                        }
                    }
                    if (keyExist&&(user.equals(ticketUser))) {
                        serverSender.send(clientSocket,(String.valueOf(key)), 3);
                        Ticket ticket = (Ticket) serverReceiver.receive(clientSocket);
                        if (dBworking.ticketExist(key)) {
                            TicketCollection.getLock().writeLock().lock();
                            ticket.setUser(user);
                            ticket.getPerson().setPassportID(ticketid);
                            ticket.setCreationDate(Timestamp.from(Instant.now()));
                            ticketCollection.putTicket(key, ticket);
                            serverSender.send(clientSocket, "Элемент с ключом " + key + " обновлён.", 0);
                            dBworking.uploadAllTickets();
                            TicketCollection.getLock().writeLock().unlock();
                        }
                        else  serverSender.send(clientSocket,"Элемент,видимо,был удалён из базы данных,пока вы там заполняли всё,проверьте и попробуйте ещё раз.",0);
                    } else {
                        serverSender.send(clientSocket,"Элемент с заданным ключом не существует или не принадлежит вам,попробуйте ввести команду снова.", 0);
                    }
                } else serverSender.send(clientSocket,"Коллекция пуста", 0);

            }
        } catch (NumberFormatException e) {
            serverSender.send(clientSocket,"Число должно быть типа Long,попробуйте ещё раз.",2);
            this.execute(null,clientSocket,user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getInfo() {
        return "---> Обновить значения элемента по его ключу.";
    }
}
