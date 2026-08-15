# 💱 Conversor de Monedas

Proyecto full-stack: **Spring Boot** (backend) + **React/Vite** (frontend) + **Neon** (Postgres) como base de datos, usando la **Frankfurter API** (gratuita, sin API key) como fuente de tasas de cambio.

## Funciones incluidas

- 🔄 Convertir entre monedas (con tasas actuales).
- 📜 Historial de conversiones (guardado en Neon, con paginación).
- 📈 Gráfica de variación de la tasa entre dos monedas (7D / 30D / 90D).
- 🎫 Cinta de tasas en vivo tipo "bolsa de valores" en la parte superior.

---

## 1. Configurar Neon (base de datos)

1. Crea una cuenta gratuita en **https://neon.tech**.
2. Crea un **New Project** (por ejemplo, `conversor-monedas`).
3. En el dashboard del proyecto, entra a **Connection Details** y copia:
   - **Host** (algo como `ep-xxxx-xxxx.us-east-2.aws.neon.tech`)
   - **Database** (por defecto `neondb`)
   - **User** y **Password**
4. Arma tu `DB_URL` con este formato (JDBC):
   ```
   jdbc:postgresql://<host>/<database>?sslmode=require
   ```
   Ejemplo:
   ```
   jdbc:postgresql://ep-cool-water-12345.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```

No necesitas crear tablas a mano: el backend usa `spring.jpa.hibernate.ddl-auto=update`, así que Hibernate crea automáticamente la tabla `conversion_history` la primera vez que arranca.

---

## 2. Backend (Spring Boot)

Requisitos: **Java 17+** y **Gradle**.

```bash
cd backend

# Configura las variables de entorno con tus datos de Neon
export DB_URL="jdbc:postgresql://<host>/<database>?sslmode=require"
export DB_USERNAME="tu_usuario_neon"
export DB_PASSWORD="tu_password_neon"
export CORS_ORIGIN="http://localhost:5173"

gradle bootRun
```

El backend queda corriendo en **http://localhost:8080**.

Para compilar / generar el `.jar`:
```bash
gradle build          # compila, corre tests y genera el jar en build/libs/
gradle build -x test  # si quieres saltarte los tests
java -jar build/libs/monedas-1.0.0.jar
```

> 💡 Recomendado: una vez tengas Gradle instalado, corre `gradle wrapper` dentro de `backend/` una sola vez. Esto genera los archivos `gradlew` / `gradlew.bat`, que te permiten compilar el proyecto sin depender de tener Gradle instalado globalmente (usarías `./gradlew bootRun` o `gradlew.bat bootRun` en vez de `gradle bootRun`). Así el proyecto queda "autocontenido" para cualquiera que lo clone.

### Endpoints principales

| Método | Ruta                                    | Descripción                                      |
|--------|------------------------------------------|---------------------------------------------------|
| GET    | `/api/currencies`                        | Lista de monedas soportadas                       |
| GET    | `/api/rates?base=USD`                    | Tasas actuales desde una moneda base (ticker)      |
| POST   | `/api/convert`                           | Convierte monto y guarda en el historial          |
| GET    | `/api/history?page=0&size=10`            | Historial de conversiones (paginado)               |
| GET    | `/api/variation?from=USD&to=EUR&start=&end=` | Serie histórica de tasas para la gráfica       |

Ejemplo body de `POST /api/convert`:
```json
{ "from": "USD", "to": "EUR", "amount": 100 }
```

---

## 3. Frontend (React + Vite)

Requisitos: **Node.js 18+**.

```bash
cd frontend
npm install

# Copia el archivo de ejemplo y ajústalo si tu backend no corre en localhost:8080
cp .env.example .env

npm run dev
```

El frontend queda en **http://localhost:5173** y ya apunta al backend local por defecto.

---

## 4. Estructura del proyecto

```
conversor-monedas/
├── backend/                # Spring Boot
│   └── src/main/java/com/conversor/monedas/
│       ├── config/         # CORS, WebClient, manejo de errores
│       ├── controller/     # CurrencyController (REST API)
│       ├── dto/             # Objetos de transferencia
│       ├── model/          # Entidad JPA (ConversionHistory)
│       ├── repository/     # Spring Data JPA
│       └── service/        # Lógica de negocio + consumo de Frankfurter API
└── frontend/                # React + Vite + Tailwind
    └── src/
        ├── api/             # Cliente Axios
        └── components/      # TickerTape, ConverterCard, HistoryTable, RateChart
```

---

## 5. Próximos pasos sugeridos

- Agregar autenticación para que el historial sea por usuario.
- Cachear `/api/currencies` y `/api/rates` (cambian poco) para reducir llamadas a Frankfurter.
- Desplegar el backend (Render/Railway/Fly.io) y el frontend (Vercel/Netlify), apuntando `VITE_API_URL` al backend desplegado.
