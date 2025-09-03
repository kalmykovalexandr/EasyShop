# EasyShop Deployment Guide

Это руководство поможет вам настроить автоматический деплой приложения EasyShop на Oracle Cloud.

## 🏗️ Архитектура CI/CD

```
GitHub Repository → GitHub Actions → Oracle Cloud Server
     ↓                    ↓                    ↓
   Code Push → Build & Test → Deploy to Production
```

## 📋 Предварительные требования

### 1. Oracle Cloud Server
- Создайте виртуальную машину в Oracle Cloud
- Рекомендуемая конфигурация: 2 vCPU, 4GB RAM, 50GB storage
- Операционная система: Oracle Linux 8 или CentOS 8
- Откройте порты: 22, 80, 8080, 9001-9003, 5432

### 2. GitHub Repository
- Убедитесь, что ваш код находится в GitHub репозитории
- У вас есть права на настройку GitHub Actions

## 🚀 Настройка сервера Oracle Cloud

### Шаг 1: Подключение к серверу
```bash
ssh opc@your-oracle-server-ip
```

### Шаг 2: Запуск скрипта настройки
```bash
# Скачайте и запустите скрипт настройки
curl -O https://raw.githubusercontent.com/your-username/EasyShop/main/infra/setup-oracle-server.sh
chmod +x setup-oracle-server.sh
./setup-oracle-server.sh
```

### Шаг 3: Клонирование репозитория
```bash
cd /opt/easyshop
git clone https://github.com/your-username/EasyShop.git .
```

## 🔐 Настройка GitHub Secrets

Перейдите в настройки вашего GitHub репозитория: `Settings → Secrets and variables → Actions`

Добавьте следующие секреты:

### Database Secrets
- `DB_USER` - имя пользователя базы данных (например: `easyshop_user`)
- `DB_PASSWORD` - пароль базы данных (используйте сложный пароль)

### JWT Secrets
- `JWT_SECRET` - секретный ключ для JWT (минимум 32 символа)
- `JWT_TTL_MINUTES` - время жизни токена в минутах (по умолчанию: 60)

### Oracle Cloud Secrets
- `ORACLE_HOST` - IP адрес вашего Oracle Cloud сервера
- `ORACLE_USERNAME` - имя пользователя (обычно `opc`)
- `ORACLE_SSH_KEY` - приватный SSH ключ для подключения к серверу
- `ORACLE_PORT` - SSH порт (по умолчанию: 22)
- `ORACLE_APP_PATH` - путь к приложению на сервере (по умолчанию: `/opt/easyshop`)

### Docker Registry Secrets
- `DOCKER_USERNAME` - ваш GitHub username
- `DOCKER_PASSWORD` - GitHub Personal Access Token с правами на packages

## 📁 Структура файлов

```
.github/workflows/
├── backend-tests.yml          # Тестирование backend
├── build-images.yml           # Сборка Docker образов
├── deploy-oracle.yml          # Деплой на Oracle Cloud
├── publish-auth-service.yml   # Публикация auth-service
├── publish-product-service.yml # Публикация product-service
├── publish-purchase-service.yml # Публикация purchase-service
└── publish-api-gateway.yml    # Публикация api-gateway

infra/
├── docker-compose.dev.yml     # Development окружение
├── docker-compose.prod.yml    # Production окружение
├── setup-oracle-server.sh     # Скрипт настройки сервера
└── env.prod.example           # Пример production переменных
```

## 🔄 Процесс деплоя

### Автоматический деплой
1. **Push в main/master ветку** → автоматически запускается деплой
2. **GitHub Actions выполняет:**
   - Запуск тестов
   - Сборку Docker образов
   - Публикацию в GitHub Container Registry
   - Деплой на Oracle Cloud сервер

### Ручной деплой
1. Перейдите в `Actions` в вашем GitHub репозитории
2. Выберите workflow `Deploy to Oracle Cloud`
3. Нажмите `Run workflow`

## 🐳 Docker Images

Все образы публикуются в GitHub Container Registry:
- `ghcr.io/your-username/easyshop/auth-service:latest`
- `ghcr.io/your-username/easyshop/product-service:latest`
- `ghcr.io/your-username/easyshop/purchase-service:latest`
- `ghcr.io/your-username/easyshop/api-gateway:latest`
- `ghcr.io/your-username/easyshop/frontend:latest`

## 🌐 Доступ к приложению

После успешного деплоя приложение будет доступно по адресу:
- **Frontend**: `http://your-oracle-server-ip`
- **API Gateway**: `http://your-oracle-server-ip:8080`
- **Auth Service**: `http://your-oracle-server-ip:9001`
- **Product Service**: `http://your-oracle-server-ip:9002`
- **Purchase Service**: `http://your-oracle-server-ip:9003`

## 🔧 Управление приложением

### Проверка статуса
```bash
cd /opt/easyshop
docker compose -f infra/docker-compose.prod.yml ps
```

### Просмотр логов
```bash
# Все сервисы
docker compose -f infra/docker-compose.prod.yml logs

# Конкретный сервис
docker compose -f infra/docker-compose.prod.yml logs auth-service
```

### Перезапуск приложения
```bash
cd /opt/easyshop
docker compose -f infra/docker-compose.prod.yml restart
```

### Остановка приложения
```bash
cd /opt/easyshop
docker compose -f infra/docker-compose.prod.yml down
```

## 🚨 Troubleshooting

### Проблема: SSH подключение не работает
- Проверьте, что SSH ключ правильно добавлен в GitHub Secrets
- Убедитесь, что порт 22 открыт в firewall Oracle Cloud

### Проблема: Docker образы не загружаются
- Проверьте, что GitHub Token имеет права на packages
- Убедитесь, что DOCKER_USERNAME и DOCKER_PASSWORD правильно настроены

### Проблема: Приложение не запускается
- Проверьте логи: `docker compose -f infra/docker-compose.prod.yml logs`
- Убедитесь, что все переменные окружения правильно настроены
- Проверьте, что база данных доступна

### Проблема: Порты не открыты
- Убедитесь, что firewall настроен правильно
- Проверьте Security Lists в Oracle Cloud Console

## 📊 Мониторинг

### Health Checks
- **Database**: `pg_isready -U $DB_USER -d easyshop`
- **Services**: HTTP endpoints доступны на соответствующих портах

### Логи
Все логи сохраняются в Docker и доступны через:
```bash
docker compose -f infra/docker-compose.prod.yml logs -f
```

## 🔄 Обновление приложения

Для обновления приложения просто сделайте push в main ветку:
```bash
git add .
git commit -m "Update application"
git push origin main
```

GitHub Actions автоматически:
1. Запустит тесты
2. Соберет новые образы
3. Задеплоит обновление на сервер

## 📝 Дополнительные настройки

### SSL/HTTPS
Для продакшена рекомендуется настроить SSL сертификат через Let's Encrypt или Oracle Cloud Load Balancer.

### Backup
Настройте автоматический backup базы данных:
```bash
# Создайте cron job для ежедневного backup
0 2 * * * docker exec infra-db-1 pg_dump -U $DB_USER easyshop > /opt/backups/easyshop_$(date +\%Y\%m\%d).sql
```

### Мониторинг
Рекомендуется настроить мониторинг с помощью Prometheus + Grafana или использовать Oracle Cloud Monitoring.
