//calendarCheck.js

document.addEventListener('DOMContentLoaded', function() {
    // HTML에서 설정한 isLoggedIn 변수를 사용
    if (typeof isLoggedIn !== 'undefined' && !isLoggedIn) {
        alert("로그인 후 사용 가능한 기능입니다.");
        return; // 로그인하지 않은 경우 캘린더 초기화를 중단
    }


    const adjustDays = parseInt(document.getElementById('adjustDays').textContent, 10);
    const adjustTime = document.getElementById('adjustTime').textContent;

    const startDateStr = document.getElementById('startDate').value;
    const endDateStr = document.getElementById('endDate').value;

    let startDate = new Date(startDateStr);
    let endDate = new Date(endDateStr);

    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(23, 59, 59, 999);

    window.calendarConfig = {
        adjustDays: adjustDays,
        adjustTime: adjustTime
    };

    initializeCalendar(startDate, endDate);
});

function initializeCalendar(startDate, endDate) {
    const calendarContainer = document.querySelector('.calendar');
    const daysInMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate();
    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);

    calendarContainer.querySelectorAll('div:not(.header)').forEach(el => el.remove());

    days.forEach(day => {
        const cell = document.createElement('div');
        cell.textContent = day;

        const cellDate = new Date(new Date().getFullYear(), new Date().getMonth(), day);

        if (cellDate >= startDate && cellDate <= endDate) {
            cell.classList.add('highlight');
        }

        cell.addEventListener('click', handleClick);
        calendarContainer.appendChild(cell);
    });
}

function handleClick(event) {
    const cell = event.currentTarget;
    const maxChecks = window.calendarConfig.adjustDays;

    if (!cell.classList.contains('highlight')) {
        return;
    }

    if (cell.classList.contains('checked')) {
        cell.classList.remove('checked');
        const icon = cell.querySelector('.check-icon');
        if (icon) {
            icon.remove();
        }
        return;
    }

    const checkedCount = document.querySelectorAll('.calendar .checked').length;

    if (checkedCount >= maxChecks) {
        return;
    }

    cell.classList.add('checked');
    const icon = document.createElement('div');
    icon.classList.add('check-icon', 'visible');
    icon.innerHTML = '✔';
    cell.appendChild(icon);
}
