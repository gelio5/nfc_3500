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
    let tag_uid = document.getElementById('abc_Tag_UID').value;
    let part_num = document.getElementById('abc_part_num').value;
    let lot_num = document.getElementById('abc_lot_num').value;
    let expiration_date = document.getElementById('abc_expiration_date').value;
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

const get_polymer_blob = async () => {
    let polymer_type = document.getElementById('polymer_type').value;
    let tag_uid = document.getElementById('pol_Tag_UID').value;
    let part_num = document.getElementById('pol_part_num').value;
    let lot_num = document.getElementById('pol_lot_num').value;
    let expiration_date = document.getElementById('pol_expiration_date').value;
    let tag_info = {
    "polymer_type": polymer_type,
    "tag_uid": tag_uid,
    "part_number": part_num,
    "lot_number": lot_num,
    "expiration_date": expiration_date}
    console.log(tag_info);
    let blob = await get_blob("/rfid/polymer_blob", tag_info);
    if (blob){
        document.getElementById("pol_result").innerText = blob;
    }
};

const get_conditioner_blob = async () => {
    let polymer_type = "Conditioner";
    let tag_uid = document.getElementById('cond_Tag_UID').value;
    let part_num = document.getElementById('cond_part_num').value;
    let lot_num = document.getElementById('cond_lot_num').value;
    let expiration_date = document.getElementById('cond_expiration_date').value;
    let tag_info = {
    "polymer_type": polymer_type,
    "tag_uid": tag_uid,
    "part_number": part_num,
    "lot_number": lot_num,
    "expiration_date": expiration_date}
    console.log(tag_info);
    let blob = await get_blob("/rfid/polymer_blob", tag_info);
    if (blob){
        document.getElementById("cond_result").innerText = blob;
    }
};

const saveBinary = (type) => {
    let tag_uid = document.getElementById(type + '_Tag_UID').value.replaceAll(" ", "_");
    let filename = tag_uid + "_blob.bin";
    let data = document.getElementById(type + '_result').innerText;
    encoder = new TextEncoder();
    data = encoder.encode(data);
    buffer = new ArrayBuffer(data.length + (4 - data.length % 4));
    let export_data = new Uint8Array(buffer);
    export_data.set(data);
    const blob = new Blob([export_data], {type: 'text/bin'});
    if(window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, filename);
    }
    else{
        const elem = window.document.createElement('a');
        elem.href = window.URL.createObjectURL(blob);
        elem.download = filename;
        document.body.appendChild(elem);
        elem.click();
        document.body.removeChild(elem);
    }
}