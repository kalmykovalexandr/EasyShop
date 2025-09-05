# EasyShop Oracle Server Update Instructions

## 🚀 Обновление приложения на сервере Oracle

### Вариант 1: Безопасное обновление (рекомендуется)

Этот вариант обновит существующее приложение, сохранив настройки и данные.

#### Шаги:

1. **Подключитесь к серверу Oracle:**
   ```bash
   ssh ubuntu@your-oracle-server-ip
   ```

2. **Перейдите в папку с проектом:**
   ```bash
   cd /opt/easyshop
   ```

3. **Скачайте скрипт обновления:**
   ```bash
   # Если у вас есть доступ к GitHub
   git pull origin main
   
   # Или скачайте файл напрямую
   wget https://raw.githubusercontent.com/your-username/EasyShop/main/infra/manual-deploy/update-oracle.sh
   ```

4. **Сделайте скрипт исполняемым:**
   ```bash
   chmod +x infra/manual-deploy/update-oracle.sh
   ```

5. **Запустите обновление:**
   ```bash
   ./infra/manual-deploy/update-oracle.sh
   ```

#### Что делает скрипт:
- ✅ Создает резервную копию текущего развертывания
- ✅ Останавливает и удаляет старые контейнеры
- ✅ Подтягивает последние изменения из GitHub
- ✅ Восстанавливает настройки окружения
- ✅ Разворачивает новую версию с улучшенной безопасностью
- ✅ Проверяет работоспособность всех сервисов

---

### Вариант 2: Полная очистка и переустановка

Этот вариант полностью удалит приложение и установит его заново.

#### Шаги:

1. **Подключитесь к серверу Oracle:**
   ```bash
   ssh ubuntu@your-oracle-server-ip
   ```

2. **Скачайте скрипт очистки:**
   ```bash
   wget https://raw.githubusercontent.com/your-username/EasyShop/main/infra/manual-deploy/clean-oracle.sh
   chmod +x clean-oracle.sh
   ```

3. **Запустите полную очистку:**
   ```bash
   ./clean-oracle.sh
   ```

4. **Клонируйте репозиторий заново:**
   ```bash
   git clone https://github.com/your-username/EasyShop.git /opt/easyshop
   cd /opt/easyshop
   ```

5. **Настройте переменные окружения:**
   ```bash
   cp infra/env.prod.example infra/.env.prod
   nano infra/.env.prod
   ```

6. **Разверните приложение:**
   ```bash
   cd infra/manual-deploy
   chmod +x deploy-oracle.sh
   ./deploy-oracle.sh
   ```

---

## 🔧 Ручное обновление (если скрипты не работают)

### 1. Остановка старых сервисов

```bash
cd /opt/easyshop
docker-compose -f docker-compose.prod.yml down
```

### 2. Удаление старых образов

```bash
docker images --filter "reference=*easyshop*" --format "{{.Repository}}:{{.Tag}}" | xargs -r docker rmi -f
```

### 3. Обновление кода

```bash
git pull origin main
```

### 4. Пересборка и запуск

```bash
docker-compose -f docker-compose.prod.yml up --build -d
```

---

## 🛠️ Устранение проблем

### Проблема: Скрипт не запускается
```bash
# Проверьте права доступа
ls -la infra/manual-deploy/update-oracle.sh

# Установите права
chmod +x infra/manual-deploy/update-oracle.sh
```

### Проблема: Docker не запущен
```bash
# Запустите Docker
sudo systemctl start docker
sudo systemctl enable docker
```

### Проблема: Недостаточно прав
```bash
# Добавьте пользователя в группу docker
sudo usermod -aG docker $USER
newgrp docker
```

### Проблема: Конфликт портов
```bash
# Проверьте занятые порты
sudo netstat -tlnp | grep -E ':(80|8080|9001|9002|9003|5432)'

# Остановите конфликтующие сервисы
sudo systemctl stop nginx  # если используется
```

---

## 📋 Проверка после обновления

### 1. Проверка статуса сервисов
```bash
docker-compose -f docker-compose.prod.yml ps
```

### 2. Проверка логов
```bash
docker-compose -f docker-compose.prod.yml logs -f
```

### 3. Проверка доступности
```bash
# Frontend
curl -I http://localhost

# API Gateway
curl -I http://localhost:8080/healthz

# Auth Service
curl -I http://localhost:9001/healthz
```

### 4. Тестирование безопасности
```bash
# Запустите тесты безопасности
./test-security.sh
```

---

## 🔒 Новые функции безопасности

После обновления у вас будет:

### Frontend:
- ✅ Улучшенная система авторизации
- ✅ Защита маршрутов по ролям
- ✅ Автоматический logout при истечении токена
- ✅ Валидация паролей
- ✅ Улучшенный UI с отображением статуса пользователя

### Backend:
- ✅ Сильные требования к паролям
- ✅ JWT токены с refresh механизмом
- ✅ Улучшенная валидация
- ✅ Новые эндпоинты для безопасности
- ✅ Лучшая обработка ошибок

---

## 📞 Поддержка

Если возникли проблемы:

1. **Проверьте логи:**
   ```bash
   docker-compose -f docker-compose.prod.yml logs
   ```

2. **Проверьте статус контейнеров:**
   ```bash
   docker ps -a
   ```

3. **Проверьте использование ресурсов:**
   ```bash
   docker stats
   ```

4. **Восстановите из резервной копии:**
   ```bash
   # Если что-то пошло не так
   sudo cp -r /opt/easyshop-backup-* /opt/easyshop
   cd /opt/easyshop
   docker-compose -f docker-compose.prod.yml up -d
   ```

---

## 🎯 Рекомендации

1. **Всегда делайте резервные копии** перед обновлением
2. **Тестируйте на тестовом сервере** перед продакшеном
3. **Мониторьте логи** после обновления
4. **Проверяйте безопасность** с помощью тестов
5. **Документируйте изменения** в конфигурации

Удачного обновления! 🚀
