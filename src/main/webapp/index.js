
document.getElementById("imageForm").addEventListener("submit", function(event) {
    event.preventDefault();
    var urlInput = document.getElementById("urlInput");
    var errorMessage = document.getElementById("errorMessage");
    var output = document.getElementById("output");
    var loading = document.getElementById("loading");

    errorMessage.textContent = "";
    output.textContent = "";
    loading.style.display = "block";

    var url = urlInput.value.trim();

    var urlPattern = /^(https?:\/\/)?([\w-]+(\.[\w-]+)+)(\/.*)?$/;
    if (!url || !urlPattern.test(url)) {
        errorMessage.textContent = "Invalid or empty URL!";
        urlInput.classList.add("error");
        output.textContent = "No images found.";
        loading.style.display = "none";
        return;
    } else {
        urlInput.classList.remove("error");
    }

    fetch('/main?url=' + encodeURIComponent(url), { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            loading.style.display = "none";  

            if (Array.isArray(data) && data.length === 1) {
                let message = data[0];

                if (message === "Crawling is not allowed for this URL") {
                    errorMessage.textContent = message;
                    output.textContent = "";
                    return;
                } else if (message.startsWith("Error:")) {
                    errorMessage.textContent = message;
                    output.textContent = "";
                    return;
                }
				else if (message === "URL already visited"){
					errorMessage.textContent = message;
					output.textContent = "";
					return;
				}
				else if(message === "No images found"){
					errorMessage.textContent = message;
					output.textContent = "";
					return;
				}
            }

            output.textContent = JSON.stringify(data, null, 2);
        })
        .catch(error => {
            loading.style.display = "none";
            errorMessage.textContent = "Error fetching images!";
            output.textContent = "";
        });
});
