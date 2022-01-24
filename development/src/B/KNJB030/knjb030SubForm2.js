//選択ボタン押し下げ時の処理
function btn_submit() {

	var chk = document.forms[0]['CHECK\[\]'];
	var chk_fuku = document.forms[0]['CHECK_FUKU\[\]'];
	var sep = sep1 = sep2 = "";
	var Ch_val  = "";	//職員コード
	var Ch_val1 = "";	//担任区分
	var Ch_txt  = "";	//副担任名
	var Ch_txt1 = "";	//正担任名
	var Ch_val2 = "";	//職員コード＋'-'＋担任区分
	var kubun   = "";

	for (var i=0;i<chk.length;i++)
	{
		if(chk[i].checked){
            var tmp = chk[i].value.split(',');

			if(chk_fuku[i].checked){
				Ch_txt 	= Ch_txt + sep2 + tmp[1];
				sep2 	= ",";
				kubun 	= "0";
			} else {
				Ch_txt1 = Ch_txt1 + sep1 + tmp[1];
				sep1 	= ",";
				kubun 	= "1";
			}

			Ch_val  = Ch_val  + sep + tmp[0];
			Ch_val1 = Ch_val1 + sep + kubun;
			Ch_val2 = Ch_val2 + sep + tmp[0] + "-" + kubun;
			sep = ",";
		}
	}
	if(Ch_val==""){
		if(chk.checked){
            var tmp = chk.value.split(',');

			if(chk_fuku.checked){
				Ch_txt 	= Ch_txt + sep2 + tmp[1];
				sep2 	= ",";
				kubun 	= "0";
			} else {
				Ch_txt1 = Ch_txt1 + sep1 + tmp[1];
				sep1 	= ",";
				kubun 	= "1";
			}

			Ch_val  = Ch_val  + sep + tmp[0];
			Ch_val1 = Ch_val1 + sep + kubun;
			Ch_val2 = Ch_val2 + sep + tmp[0] + "-" + kubun;
			sep = ",";
		}
	}
	top.main_frame.right_frame.document.forms[0].STAFFCD.value 			= Ch_val;
	top.main_frame.right_frame.document.forms[0].CHARGEDIV.value 		= Ch_val1;
	top.main_frame.right_frame.document.forms[0].STAFFNAME_SHOW1.value 	= Ch_txt1;
	top.main_frame.right_frame.document.forms[0].STAFFNAME_SHOW.value 	= Ch_txt;
	top.main_frame.right_frame.document.forms[0].STF_CHARGE.value 		= Ch_val2;

	top.main_frame.right_frame.closeit();
}
