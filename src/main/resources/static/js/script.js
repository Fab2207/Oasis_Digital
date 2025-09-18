document.addEventListener('DOMContentLoaded', () => {
    AOS.init({
        duration: 1000,
        once: true,
        offset: 120
    });

    const weatherTemp = document.getElementById('weather-temp');
    const weatherIcon = document.querySelector('.weather-info i');
    const currentTimeSpan = document.getElementById('current-time');
    const currentDateSpan = document.getElementById('current-date');

    function updateDateTime() {
        const now = new Date();
        const timeOptions = { hour: '2-digit', minute: '2-digit' };
        const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        
        const timeString = now.toLocaleTimeString('es-ES', timeOptions);
        const dateString = now.toLocaleDateString('es-ES', dateOptions);

        currentTimeSpan.textContent = timeString;
        currentDateSpan.textContent = dateString;
    }

    function getWeather() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;
                const apiKey = 'TU_CLAVE_DE_API_AQUÍ';
                const apiUrl = `https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiKey}&units=metric&lang=es`;

                fetch(apiUrl)
                    .then(response => response.json())
                    .then(data => {
                        const temp = Math.round(data.main.temp);
                        const weather = data.weather[0].main.toLowerCase();

                        weatherTemp.textContent = `${temp}°C`;
                        
                        if (weather.includes('clear')) {
                            weatherIcon.className = 'fas fa-sun';
                        } else if (weather.includes('cloud')) {
                            weatherIcon.className = 'fas fa-cloud';
                        } else if (weather.includes('rain')) {
                            weatherIcon.className = 'fas fa-cloud-showers-heavy';
                        } else if (weather.includes('snow')) {
                            weatherIcon.className = 'fas fa-snowflake';
                        } else if (weather.includes('thunderstorm')) {
                            weatherIcon.className = 'fas fa-bolt';
                        }
                    })
                    .catch(error => {
                        weatherTemp.textContent = 'N/A';
                    });
            }, error => {
                weatherTemp.textContent = 'N/A';
            });
        } else {
            weatherTemp.textContent = 'N/A';
        }
    }

    updateDateTime();
    getWeather();
    setInterval(updateDateTime, 60000);
});