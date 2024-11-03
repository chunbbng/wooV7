// 로그인 상태를 체크하는 함수
function checkLoginStatus() {
    return fetch('/check-login') // 로그인 상태 확인을 위한 엔드포인트 호출
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => data.isLoggedIn);
}

// 메뉴 클릭 시 로그인 상태 체크
function setupMenuLinks() {
    const menuLinks = document.querySelectorAll('.menu a'); // 메뉴 링크 선택

    menuLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            const path = link.getAttribute('href');

            // "Cabinet", "Calendar", "Check" 페이지 접근 시 로그인 체크
            if (path === '/cabinet' || path === '/calendar' || path === '/check') {
                event.preventDefault(); // 기본 동작 방지
                checkLoginStatus().then(isLoggedIn => {
                    if (isLoggedIn) {
                        window.location.href = path; // 로그인되어 있으면 페이지 이동
                    } else {
                        alert("로그인된 사용자만 이용 가능합니다!");
                        window.location.href = "/"; // index.html로 리다이렉트
                    }
                });
            }
        });
    });
}

// 헤더의 스크롤 이벤트 처리
function handleHeaderScroll() {
    let lastScrollTop = 0;
    const header = document.querySelector('.header'); // 헤더 선택
    const scrollThreshold = 270; // 스크롤을 150px 이상 내리면 헤더가 사라짐

    // 헤더가 사라지는 속도를 더 느리게 조정
    header.style.transition = 'transform 0.6s ease-in-out';

    window.addEventListener('scroll', function() {
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        if (scrollTop > scrollThreshold) {
            if (scrollTop > lastScrollTop) {
                // 스크롤을 아래로 내리면 헤더를 숨김
                header.classList.add('hidden');
            } else {
                // 스크롤을 위로 올리면 헤더를 표시
                header.classList.remove('hidden');
            }
        } else {
            // 스크롤이 임계값보다 적으면 항상 헤더 표시
            header.classList.remove('hidden');
        }

        lastScrollTop = scrollTop;
    });
}

// 페이지 로드 시 추가 설정
document.addEventListener('DOMContentLoaded', () => {
    setupMenuLinks(); // 메뉴 링크 설정
    handleHeaderScroll(); // 헤더 스크롤 설정
});