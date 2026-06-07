## Navigation
- [About project](#о-проекте)
- [How to start](#как-запустить)
- [Prerequisites](#предварительные-требования)
- [Setup environment](#настройка-окружения)
- [Launching a Database](#запуск-базы-данных)
- [Starting an App](#запуск-приложения)
- [Functionality](#функциональность)
- [Database](#-база-данных)
- [Database structure](#структура-базы-данных)
- [Detection mechanism](#механизм-отслеживания)
- [Configuration](#конфигурация)
- [Spring project](#spring-проект)
- [Project Structure](#-структура-проекта)
- [Error Handling](#обработка-ошибок)
- [Testing](#-тестирование)
- [Docker Compose File](#docker-compose-файл)
- [Fixing problems](#-устранение-неполадок)
- [Licensing](#-license)
- [Authors](#-authors)

## О проекте
Telegram бот для отслеживания курса биткоина с возможностью подписки на желаемую цену.

## Как запустить

При открытии директории через IntelliJ IDEA проект должен автоматически распознаться.
У вас должна появится `run configuration CryptoBotApplication` (зеленый треугольник),
если не появился, можно пройти в класс `CryptoBotApplication` и оттуда напрямую вызвать `main` метод.

### Предварительные требования

1. **Docker и Docker Compose** - для запуска базы данных PostgreSQL
2. **Telegram Bot Token** - получите у [@BotFather](https://t.me/BotFather)
3. **Java 17** - необходима для запуска приложения

### Настройка окружения

Создайте файл `.env` в корне проекта или установите переменные среды:

```env
BOT_NAME=your_bot_username
BOT_TOKEN=your_bot_token
```
### Запуск базы данных
```shell
docker-compose up -d
```
Это запустит PostgreSQL на порту 5432 с параметрами:
База данных: `db`
Пользователь: `root`
Пароль: `root`

### Запуск приложения
```shell
./gradlew bootRun
```
Или через **IntelliJ IDEA**: запустите `main` метод в `CryptoBotApplication`

### Функциональность
Бот поддерживает следующие команды:

- `/start` - регистрация пользователя и вывод справки
- `/get_price` - получить текущую стоимость биткоина
- `/subscribe [цена]` - подписаться на стоимость биткоина (например: `/subscribe 50000`)
- `/get_subscription` - показать текущую подписку
- `/unsubscribe` - отменить подписку

### 🗄️ База данных
Для удобной работы с **postgreSql** используется докер-образ,
вам не нужно самостоятельно устанавливать **postgreSql**.
Выполнив `docker-compose up`, вы запустите **postgreSql** с предустановленными
настройками (их посмотреть можно в файле `docker-compose.yaml`).

#### Структура базы данных:

Таблица `subscribers`:

- `id` - уникальный идентификатор записи
- `telegram_id` - ID пользователя в Telegram (уникальный)
- `subscription_price` - цена, на которую подписан пользователь (NULL если нет подписки)
- `last_notification_time` - время последнего уведомления

### Механизм отслеживания

- Бот каждые 2 минуты запрашивает актуальную стоимость биткоина
- Проверяет пользователей, у которых желаемая цена >= текущей
- Отправляет уведомление "Пора покупать, стоимость биткоина ХХХ"
- **Важно:** уведомления приходят не чаще 1 раза в 10 минут для каждого пользователя

### Конфигурация

В `application.yml` находятся конфигурируемые параметры приложения:
```yaml
scheduler:
  price-check:
    fixed-delay: 120000  # Частота проверки цены (2 минуты)

notification:
  cooldown:
    minutes: 10  # Минимальный интервал между уведомлениями
```

### Spring проект

В данном проекте используется **Spring**, **gradle** используется в качестве системы сборки.
Основные зависимости уже указаны, но никто вам не запрещает добавлять новые.

**Используемые технологии:**

- Spring Boot 3.1.5
- Spring Data JPA (Hibernate)
- PostgreSQL
- Telegram Bot API
- Gradle
- Docker / Docker Compose

### 📁 Структура проекта
```text
src/main/java/com/skillbox/cryptobot/
├── bot/
│   ├── CryptoBot.java              # Главный класс бота
│   └── command/                    # Обработчики команд
│       ├── GetPriceCommand.java
│       ├── SubscribeCommand.java
│       ├── GetSubscriptionCommand.java
│       └── UnsubscribeCommand.java
├── client/
│   └── BinanceClient.java          # Клиент для API Binance
├── configuration/
│   └── TelegramBotConfiguration.java
├── entity/
│   └── Subscriber.java             # Entity для БД
├── repository/
│   └── SubscriberRepository.java   # JPA репозиторий
├── service/
│   ├── CryptoCurrencyService.java  # Сервис для работы с ценой
│   └── NotificationService.java    # Сервис уведомлений
└── utils/
    └── TextUtil.java               # Вспомогательные утилиты
```

### Обработка ошибок
Приложение корректно обрабатывает:

Некорректный ввод цены (проверка регулярными выражениями)
Отсутствие подписки при запросе информации
Ошибки сети при обращении к Binance API
Ошибки базы данных

### Разработка
Для добавления новых функций:

1. Создайте новый класс команды, реализующий `IBotCommand`
2. Зарегистрируйте команду в `CryptoBot` (автоматически через `List<IBotCommand>`)
3. Добавьте необходимые сервисы через Dependency Injection

### 🧪 Тестирование

Проверить работу можно:

1. Запустить бота
2. Найти бота в Telegram по username
3. Отправить команду `/start`
4. Попробовать подписаться: `/subscribe 100000`
5. Проверить подписку: `/get_subscription`
6. Отменить подписку: `/unsubscribe`

### Docker Compose файл

Создайте файл `docker-compose.yaml` в корне проекта:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: db
      POSTGRES_USER: root
      POSTGRES_PASSWORD: root
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### ⚠️ Устранение неполадок

**Проблема: Бот не отвечает**

- Проверьте переменные окружения BOT_NAME и BOT_TOKEN
- Убедитесь, что бот запущен и нет ошибок в логах

**Проблема: Ошибка подключения к БД**

- Проверьте, запущен ли Docker: `docker ps`
- Запустите контейнер: `docker-compose up -d`
- Проверьте доступность порта **5432**

**Проблема: Не приходят уведомления**

- Проверьте, что подписка активна: `/get_subscription`
- Убедитесь, что текущая цена ниже цены подписки
- Проверьте интервал между уведомлениями (10 минут)

### 📄 License
This project uses Apache License 2.0 license

### 👤 Authors
Updated by LogicDarkCreator

[Original code](https://github.com/skillbox-java/p3_currency_telegram_bot)