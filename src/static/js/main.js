const base_url = document.location.hostname + ":" + document.location.port


const get_blob = async (url = "", data = {}) => {
    // Default options are marked with *
    const response = await fetch(url, {
      method: "POST", // *GET, POST, PUT, DELETE, etc.
    //   mode: "same-origin", // no-cors, *cors, same-origin
    //   cache: "no-cache", // *default, no-cache, reload, force-cache, only-if-cached
    //   credentials: "same-origin", // include, *same-origin, omit
      headers: {
        "accept": "application/json",
        "Content-Type": "application/json",
        // 'Content-Type': 'application/x-www-form-urlencoded',
      },
      redirect: "follow", // manual, *follow, error
      referrerPolicy: "no-referrer", // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
      mode: "cors", // no-cors, *cors, same-origin
      body: JSON.stringify(data), // body data type must match "Content-Type" header
    });
    return response.json(); // parses JSON response into native JavaScript objects
  }

const copyToClipboard = async () => {
  try {
    const element = document.querySelector(".user-select-all");
    await navigator.clipboard.writeText(element.textContent);
    // Optional: Provide feedback or perform additional actions upon successful copy
  } catch (error) {
    console.error("Failed to copy to clipboard:", error);
    // Optional: Handle and display the error to the user
  }
};


const get_anode_buffer_blob = async () => {
    let tag_uid = document.getElementById('Tag_UID').value;
    let part_num = document.getElementById('part_num').value;
    let lot_num = document.getElementById('lot_num').value;
    let expiration_date = document.getElementById('expiration_date').value;
    let tag_info = {
    "tag_uid": tag_uid,
    "part_number": part_num,
    "lot_number": lot_num,
    "expiration_date": expiration_date
    }
    console.log(tag_info);
    let blob = await get_blob("/rfid/anode_buffer_blob", tag_info);
    document.getElementById("abc_result").textContent = blob;
};

