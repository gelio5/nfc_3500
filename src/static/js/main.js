const base_url = document.location.hostname + ":" + document.location.port


const get_blob = async (url = "", data = {}) => {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "accept": "application/json",
        "Content-Type": "application/json",
      },
      redirect: "follow",
      referrerPolicy: "no-referrer",
      mode: "cors",
      body: JSON.stringify(data),
    });
    return response.json();
  }

const CopyBlobToClipboard = async (source) => {
    try {
        source = document.getElementById(source);
        await navigator.clipboard.writeText(source.textContent);
    } catch (error) {
        console.error("Failed to copy to clipboard:", error);
    }}

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
    if (blob){
        document.getElementById("abc_result").innerText = blob;
    }
};

const get_cathode_buffer_blob = async () => {
    let tag_uid = document.getElementById('cbc_Tag_UID').value;
    let part_num = document.getElementById('cbc_part_num').value;
    let lot_num = document.getElementById('cbc_lot_num').value;
    let expiration_date = document.getElementById('cbc_expiration_date').value;
    let tag_info = {
    "tag_uid": tag_uid,
    "part_number": part_num,
    "lot_number": lot_num,
    "expiration_date": expiration_date
    }
    console.log(tag_info);
    let blob = await get_blob("/rfid/cathode_buffer_blob", tag_info);
    if (blob){
        document.getElementById("cbc_result").innerText = blob;
    }
};

