document.addEventListener("DOMContentLoaded", function () {
    const toggleLoginButton = document.getElementById("toggleLogin");
    const loginBanner = document.getElementById("loginBanner");
    const loginButton = document.getElementById("loginButton");
    const signupButton = document.getElementById("signupButton");
    const welcomeMessage = document.getElementById("welcomeMessage");
    const messageDiv = document.getElementById("message");

    // 요소들이 제대로 로드되었는지 확인
    if (!toggleLoginButton || !loginBanner || !loginButton || !signupButton || !messageDiv || !welcomeMessage) {
        console.error("필수 요소가 로드되지 않았습니다.");
        return;
    }

    // 로그인 상태 확인 요청
    fetch('/auth/status')
        .then(response => response.json())
        .then(data => {
            if (data.loggedIn) {
                welcomeMessage.style.display = "block";
                welcomeMessage.classList.add("show");
                welcomeMessage.innerHTML = `${data.name}님 환영합니다!`;
                toggleLoginButton.textContent = "로그아웃";
                loginButton.style.display = "none";
                signupButton.style.display = "none";
            } else {
                toggleLoginButton.textContent = "로그인";
            }
        })
        .catch(error => {
            console.error("오류 발생:", error);
            messageDiv.innerHTML = `오류 발생: ${error.message}`;
        });

    // 로그인/로그아웃 버튼 클릭 이벤트 리스너
    toggleLoginButton.addEventListener("click", function () {
        if (toggleLoginButton.textContent === "로그아웃") {
            // 로그아웃 처리
            fetch('/auth/logout', { method: 'POST' })
                .then(() => {
                    welcomeMessage.classList.remove("show");
                    setTimeout(() => location.reload(), 500);
                })
                .catch(error => {
                    console.error("로그아웃 오류:", error);
                    messageDiv.innerHTML = `로그아웃 오류: ${error.message}`;
                });
        } else {
            loginBanner.classList.toggle("show");
        }
    });

    // 로그인 버튼 클릭 이벤트 리스너
    loginButton.addEventListener("click", function () {
        const formData = new FormData(document.getElementById("authForm"));
        fetch('/auth/login', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    welcomeMessage.classList.add("show");
                    welcomeMessage.style.display = "block";
                    welcomeMessage.style.lineHeight = "2";
                    welcomeMessage.innerHTML = `${data.message}님 환영합니다!<br>아래 항목을 기입하여 일정을 만들고<br>좌측의 다양한 메뉴를 활용해보세요`;

                    toggleLoginButton.textContent = "로그아웃";

                    loginBanner.style.transition = "none";
                    loginBanner.classList.remove("show");
                    setTimeout(() => loginBanner.style.transition = "", 50);

                    loginButton.style.display = "none";
                    signupButton.style.display = "none";

                    // 일정 시간 후 웰컴 박스를 자동으로 숨기기
                    setTimeout(() => {
                        welcomeMessage.classList.remove("show");
                    }, 2800);
                } else {
                    messageDiv.innerHTML = `로그인 실패: ${data.message}`;
                }
            })
            .catch(error => {
                console.error("로그인 오류:", error);
                messageDiv.innerHTML = `로그인 오류: ${error.message}`;
            });
    });
});