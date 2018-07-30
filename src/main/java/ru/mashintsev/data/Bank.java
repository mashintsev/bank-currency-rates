package ru.mashintsev.data;

/**
 * Created by i.mashintsev on 19.02.17.
 */
public enum Bank {
    TINKOFF("Тинькофф"), VTB24("ВТБ24"), ALFABANK("Альфа-Банк"),
    RSB("Русский стандарт"), UNICREDIT("Юникредит"), SBERBANK("Сбербанк"),
    GAZPROMBANK("Газпромбанк"), RSHB("Россельхозбанк"), BSPB("Банк Санкт-Петербург"),
    HOMECREDIT("Хоум Кредит"), RAIFFEISEN("Райффайзенбанк"), ROSBANK("Росбанк"),
    OPEN("Открытие"), MKB("Московский кредитный банк"), ROCKET("Рокетбанк");

    public final String label;

    Bank(String label) {
        this.label = label;
    }
}
