import java.util.ArrayList;
import java.util.List;

public class Warehouse {
    private List<Sock> socks = new ArrayList<>();

    // Регистрация прихода носков
    public void addIncome(String color, int cottonPart, int quantity) {
        checkParams(color, cottonPart, quantity);
        String cleanColor = color.trim().toLowerCase();

        for (Sock sock : socks) {
            if (sock.getColor().equals(cleanColor) && sock.getCottonPart() == cottonPart) {
                sock.setQuantity(sock.getQuantity() + quantity);
                return;
            }
        }

        socks.add(new Sock(cleanColor, cottonPart, quantity));
    }

    // Списание носков со склада
    public void removeOutcome(String color, int cottonPart, int quantity) {
        checkParams(color, cottonPart, quantity);
        String cleanColor = color.trim().toLowerCase();

        for (int i = 0; i < socks.size(); i++) {
            Sock sock = socks.get(i);
            if (sock.getColor().equals(cleanColor) && sock.getCottonPart() == cottonPart) {
                if (sock.getQuantity() < quantity) {
                    throw new IllegalArgumentException(
                            "Недостаточно носков на складе. Доступно: " + sock.getQuantity()
                    );
                }
                int remaining = sock.getQuantity() - quantity;
                if (remaining == 0) {
                    socks.remove(i);
                } else {
                    sock.setQuantity(remaining);
                }
                return;
            }
        }

        throw new IllegalArgumentException("Таких носков нет на складе");
    }

    // Подсчёт остатков по фильтру
    public int getSocksCount(String color, String operation, int cottonPart) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан цвет носков");
        }
        if (cottonPart < 0 || cottonPart > 100) {
            throw new IllegalArgumentException("Процент хлопка должен быть от 0 до 100");
        }
        if (operation == null ||
                (!operation.equals("moreThan") && !operation.equals("lessThan") && !operation.equals("equal"))) {
            throw new IllegalArgumentException("Неверная операция (нужно moreThan, lessThan или equal)");
        }

        String cleanColor = color.trim().toLowerCase();
        int total = 0;

        for (Sock sock : socks) {
            if (!sock.getColor().equals(cleanColor)) {
                continue;
            }
            if (operation.equals("moreThan") && sock.getCottonPart() > cottonPart) {
                total += sock.getQuantity();
            } else if (operation.equals("lessThan") && sock.getCottonPart() < cottonPart) {
                total += sock.getQuantity();
            } else if (operation.equals("equal") && sock.getCottonPart() == cottonPart) {
                total += sock.getQuantity();
            }
        }

        return total;
    }

    // Проверка общих параметров для прихода и расхода
    private void checkParams(String color, int cottonPart, int quantity) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан цвет носков");
        }
        if (cottonPart < 0 || cottonPart > 100) {
            throw new IllegalArgumentException("Процент хлопка должен быть от 0 до 100");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество пар должно быть больше 0");
        }
    }
}