package log;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LogWindowSource {
    private final int m_iQueueLength;
    private final BlockingQueue<LogEntry> m_messages;
    private final BlockingQueue<LogChangeListener> m_listeners;
    private volatile LogChangeListener[] m_activeListeners;

    public LogWindowSource(int iQueueLength) {
        m_iQueueLength = iQueueLength;
        m_messages = new ArrayBlockingQueue<>(iQueueLength);
        m_listeners = new ArrayBlockingQueue<>(iQueueLength);
    }

    public void registerListener(LogChangeListener listener) {
        m_listeners.offer(listener);
        // При регистрации нового слушателя обновляем активных слушателей
        updateActiveListeners();
    }

    public void unregisterListener(LogChangeListener listener) {
        m_listeners.remove(listener);
        // При удалении слушателя обновляем активных слушателей
        updateActiveListeners();
    }

    public void append(LogLevel logLevel, String strMessage) {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        m_messages.offer(entry);
        if (m_messages.size() > m_iQueueLength) {
            m_messages.poll();
        }
        notifyListeners();
    }

    private void notifyListeners() {
        LogChangeListener[] activeListeners = m_activeListeners;
        if (activeListeners == null) {
            // Если активные слушатели не были инициализированы, обновляем их
            updateActiveListeners();
            activeListeners = m_activeListeners;
        }
        // Проверяем наличие активных слушателей перед оповещением
        if (activeListeners != null) {
            for (LogChangeListener listener : activeListeners) {
                listener.onLogChanged();
            }
        }
    }

    private void updateActiveListeners() {
        // Обновляем массив активных слушателей
        m_activeListeners = m_listeners.toArray(new LogChangeListener[0]);
    }

    public int size() {
        return m_messages.size();
    }

    public Iterable<LogEntry> range(int startFrom, int count) {
        // Возвращаем подсписок сообщений в указанном диапазоне
        ArrayList<LogEntry> range = new ArrayList<>(m_messages);
        int end = Math.min(startFrom + count, range.size());
        return range.subList(startFrom, end);
    }

    public Iterable<LogEntry> all() {
        return new ArrayList<>(m_messages);
    }
}