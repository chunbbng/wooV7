document.addEventListener('DOMContentLoaded', function() {
    const restOptions = document.querySelectorAll('.rest-option');
    const restTimeInput = document.getElementById('restTime');

    restOptions.forEach(option => {
        option.addEventListener('click', () => {
            const time = option.getAttribute('data-time');
            restOptions.forEach(opt => opt.classList.remove('selected'));
            option.classList.add('selected');
            restTimeInput.value = time;

            // 콘솔 로그로 restTime 확인
            console.log("Selected restTime:", restTimeInput.value);
        });
    });

    const timeOptions = document.querySelectorAll('.time-option');
    const preferredTimesInput = document.getElementById('preferenceTime');
    let preferredTimes = [];

    timeOptions.forEach(option => {
        option.addEventListener('click', () => {
            const time = option.getAttribute('data-time');
            if (preferredTimes.includes(time)) {
                preferredTimes = preferredTimes.filter(t => t !== time);
                option.classList.remove('selected');
            } else {
                preferredTimes.push(time);
                option.classList.add('selected');
            }
            preferredTimesInput.value = preferredTimes.join(',');

            // 콘솔 로그로 preferenceTime 확인
            console.log("Selected preferenceTime:", preferredTimesInput.value);
        });
    });

    const selectDateButton = document.getElementById("selectDateButton");
    selectDateButton.addEventListener("click", function() {
        const restTime = restTimeInput.value;
        const preferenceTime = preferredTimesInput.value;

        if (!restTime || !preferenceTime) {
            alert("휴식 시간과 작업 선호 시간대를 선택해주세요.");
            return;
        }

        // 폼 제출 전 값 출력
        console.log("Submitting form with restTime:", restTime);
        console.log("Submitting form with preferenceTime:", preferenceTime);

        // 폼 제출
        document.getElementById("scheduleForm").submit();
    });
});