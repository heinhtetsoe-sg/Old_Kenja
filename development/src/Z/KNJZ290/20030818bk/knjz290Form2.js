//////右側でデータが更新されると、左側のリストのデータも更新する/////////////////////////////////////////////////////
function FromList(jo,se,sh,cl,hc,sc,scd,na)
{
	document.forms[0].STAFFCD.value = scd;
	document.forms[0].NAME.value = na;
//	document.forms[0].STAFFFNAME.value = cl;
	for(var a = 0; a < document.forms[0].JOBNAME.length; a++)
	{
		if(document.forms[0].JOBNAME.options[a].value == jo)
		{
			document.forms[0].JOBNAME.selectedIndex = a;
			break;
		}
	}
	for(var b = 0; b < document.forms[0].SECTIONNAME.length; b++)
	{
		if(document.forms[0].SECTIONNAME.options[b].value == se)
		{
			document.forms[0].SECTIONNAME.selectedIndex = b;
			break;
		}
	}
	for(var c = 0; c < document.forms[0].SHARENAME.length; c++)
	{
		if(document.forms[0].SHARENAME.options[c].value == sh)
		{
			document.forms[0].SHARENAME.selectedIndex = c;
			break;
		}
	}
	for(var d = 0; d < document.forms[0].CHARGECLASS.length; d++)
	{
		if(document.forms[0].CHARGECLASS.options[d].value == cl)
		{
			document.forms[0].CHARGECLASS.selectedIndex = d;
			break;
		}
	}
	for(var e = 0; e < document.forms[0].HCLUBNAME.length; e++)
	{
		if(document.forms[0].HCLUBNAME.options[e].value == hc)
		{
			document.forms[0].HCLUBNAME.selectedIndex = e;
			break;
		}
	}
	for(var f = 0; f < document.forms[0].SCLUBNAME.length; f++)
	{
		if(document.forms[0].SCLUBNAME.options[f].value == sc)
		{
			document.forms[0].SCLUBNAME.selectedIndex = f;
			break;
		}
	}

}

function btn_submit(cmd)
{
	if (cmd == 'clear')
	{
		if (!confirm('{rval MZ0003}'))
		{
			return false;
		}
	}
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
}

//////////////////////////////////「前年度からコピー」ボタン///////////////////////////////////////////////////

function btn_AddSubmit(cmd)
{
	if(document.forms[0].year.selectedIndex != 0)
	{
		alert("年度が正しくありません。");
		return false;
	}
	if(document.forms[0].staffyear.length == 0)
	{
		alert("コピーするデータが読込まれていません。");
		return false;
	}

	for (var x = 0; x < document.forms[0].staffmaster.length; x++)
	{
		wrkv = document.forms[0].staffmaster.options[x].value.split(',');
		//画面上ですでにデータが処理されていた場合はコピーしない
		if(wrkv[1] == "d")
		{
			alert('{rval MZ0026}');
			return false;
		}
	}
	for (var i = 0; i < document.forms[0].staffyear.length; i++)
	{
		mav = document.forms[0].staffyear.options[i].value.split(',');
		mat = document.forms[0].staffyear.options[i].text.split('|');

		//画面上ですでにデータが処理されていた場合はコピーしない
		if(mav[1] == "a" || mav[1] == "u")
		{
			alert('{rval MZ0026}');
			return false;
		}
	}

//	var temp1 = new Array();
//	var tempa = new Array();
//	var v = document.forms[0].year.length;
//	var w = document.forms[0].year.options[0].value;

//	var ws = eval(w);

//	document.forms[0].year.options[v] = new Option();
//	document.forms[0].year.options[v].value = ws + 1;
//	document.forms[0].year.options[v].text = ws + 1;

//	for (var i = 0; i < document.forms[0].year.length; i++)
//	{
//		temp1[i] = document.forms[0].year.options[i].value;
//		tempa[i] = document.forms[0].year.options[i].text;
//	}
//	//sort
//	temp1 = temp1.sort();
//	tempa = tempa.sort();
//	temp1 = temp1.reverse();
//	tempa = tempa.reverse();

	//generating new options
//	ClearList(document.forms[0].year,document.forms[0].year);
//	if (temp1.length>0)
//	{
//		for (var i = 0; i < temp1.length; i++)
//		{	
//			document.forms[0].year.options[i] = new Option();
//			document.forms[0].year.options[i].value = temp1[i];
//			document.forms[0].year.options[i].text =  tempa[i];
//			if(w==temp1[i]){
//				document.forms[0].year.options[i].selected=true;
//			}
//		}
//	}

	ylen = document.forms[0].staffyear.length;

	document.forms[0].copy1.value = "";
	document.forms[0].copy2.value = "";
	document.forms[0].copy3.value = "";
	document.forms[0].copy4.value = "";

	copyp = "";
	for(j=0; j<ylen; j++)
	{
		yev = document.forms[0].staffyear.options[j].value.split(',');
		document.forms[0].copy1.value = document.forms[0].copy1.value + copyp + yev[0];
		document.forms[0].copy2.value = document.forms[0].copy2.value + copyp + yev[2];
		document.forms[0].copy3.value = document.forms[0].copy3.value + copyp + yev[3];
		document.forms[0].copy4.value = document.forms[0].copy4.value + copyp + yev[5];
		copyp = ",";
	}

	document.forms[0].cmd.value = cmd;
	document.forms[0].submit();
	return false;
}


//////////////////////////////////保存（データベースへ書き込み）///////////////////////////////////////////////////
function doSubmit()
{

	ylen = document.forms[0].staffyear.length;
	mlen = document.forms[0].staffmaster.length;

	document.forms[0].addcd.value = "";
	document.forms[0].addjob.value = "";
	document.forms[0].addsec.value = "";
	document.forms[0].addsha.value = "";
	document.forms[0].addcla.value = "";
	document.forms[0].addhcl.value = "";
	document.forms[0].addscl.value = "";

	document.forms[0].updcd.value = "";
	document.forms[0].updjob.value = "";
	document.forms[0].updsec.value = "";
	document.forms[0].updsha.value = "";
	document.forms[0].updcla.value = "";
	document.forms[0].updhcl.value = "";
	document.forms[0].updscl.value = "";

	document.forms[0].delcd.value = "";
	document.forms[0].deljob.value = "";
	document.forms[0].delsec.value = "";
	document.forms[0].delsha.value = "";
	document.forms[0].delcla.value = "";
	document.forms[0].delhcl.value = "";
	document.forms[0].delscl.value = "";

	addp = "";
	updp = "";
	delp = "";
	for(j=0; j<ylen; j++)
	{
		yev = document.forms[0].staffyear.options[j].value.split(',');
		switch(yev[1])
		{
			case "a":
//				if(yev[0]=="" || yev[2]=="" || yev[3]=="" || yev[4]=="" || yev[5]=="")
				if(yev[0]=="" || yev[2]=="" || yev[3]=="" || yev[5]=="")
				{
					alert(yev[0] + "；未入力");
					return false;
				}
				document.forms[0].addcd.value = document.forms[0].addcd.value + addp + yev[0];
				document.forms[0].addjob.value = document.forms[0].addjob.value + addp + yev[2];
				document.forms[0].addsec.value = document.forms[0].addsec.value + addp + yev[3];
				document.forms[0].addsha.value = document.forms[0].addsha.value + addp + yev[4];
				document.forms[0].addcla.value = document.forms[0].addcla.value + addp + yev[5];
				document.forms[0].addhcl.value = document.forms[0].addhcl.value + addp + yev[6];
				document.forms[0].addscl.value = document.forms[0].addscl.value + addp + yev[7];
				addp = ",";
				continue;
			case "u":
//				if(yev[0]=="" || yev[2]=="" || yev[3]=="" || yev[4]=="" || yev[5]=="")
				if(yev[0]=="" || yev[2]=="" || yev[3]=="" || yev[5]=="")
				{
					alert(yev[0] + "；未入力");
					return false;
				}
				document.forms[0].updcd.value = document.forms[0].updcd.value + updp + yev[0];
				document.forms[0].updjob.value = document.forms[0].updjob.value + updp + yev[2];
				document.forms[0].updsec.value = document.forms[0].updsec.value + updp + yev[3];
				document.forms[0].updsha.value = document.forms[0].updsha.value + updp + yev[4];
				document.forms[0].updcla.value = document.forms[0].updcla.value + updp + yev[5];
				document.forms[0].updhcl.value = document.forms[0].updhcl.value + updp + yev[6];
				document.forms[0].updscl.value = document.forms[0].updscl.value + updp + yev[7];
				updp = ",";
				continue;
//			if(yev[0]=="" || yev[2]=="" || yev[3]=="" || yev[4]=="" || yev[5]=="")
			if(yev[0]=="" || yev[2]=="" || yev[3]=="" || yev[5]=="")
			{
				alert(yev[0] + "；未入力");
				return false;
			}
		}
	}
	for(k=0; k<mlen; k++)
	{
		masv = document.forms[0].staffmaster.options[k].value.split(',');
		if(masv[1] == "d")
		{
			document.forms[0].delcd.value = document.forms[0].delcd.value + delp + masv[0];
			document.forms[0].deljob.value = document.forms[0].deljob.value + delp + masv[2];
			document.forms[0].delsec.value = document.forms[0].delsec.value + delp + masv[3];
			document.forms[0].delsha.value = document.forms[0].delsha.value + delp + masv[4];
			document.forms[0].delcla.value = document.forms[0].delcla.value + delp + masv[5];
			document.forms[0].delhcl.value = document.forms[0].delhcl.value + delp + masv[4];
			document.forms[0].delscl.value = document.forms[0].delscl.value + delp + masv[5];
			delp = ",";
			continue;
		}
	}


    document.forms[0].cmd.value = 'addupddel';
    document.forms[0].submit();
    return false;
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function ClearList(OptionList, TitleName) 
{
	OptionList.length = 0;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function LastDel(string)
{
	strlen = string.len;
	strlen = strlen - 2;
	newstr = string.substr(0,strlen);
	return newstr;
}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//function add()
//{
//	var temp1 = new Array();
//	var tempa = new Array();
//	var v = document.forms[0].year.length;
//	var w = document.forms[0].year_add.value
//	
//	if (w == "")
//		return false;
//
//	for (var i = 0; i < v; i++)
//	{	
//		if (w == document.forms[0].year.options[i].value) {
//			alert("追加した年度は既に存在しています。");
//			return false;
//		}
//	}
//	document.forms[0].year.options[v] = new Option();
//	document.forms[0].year.options[v].value = w;
//	document.forms[0].year.options[v].text = w;
//	
//	for (var i = 0; i < document.forms[0].year.length; i++)
//	{  
//		temp1[i] = document.forms[0].year.options[i].value;
//		tempa[i] = document.forms[0].year.options[i].text;
//	} 
//	//sort
//	temp1 = temp1.sort();
//	tempa = tempa.sort();
//	temp1 = temp1.reverse();
//	tempa = tempa.reverse();
//	
//	//generating new options
//	ClearList(document.forms[0].year,document.forms[0].year);
//	if (temp1.length>0)
//	{	
//		for (var i = 0; i < temp1.length; i++)
//		{	
//			document.forms[0].year.options[i] = new Option();
//			document.forms[0].year.options[i].value = temp1[i];
//			document.forms[0].year.options[i].text =  tempa[i];
//			if(w==temp1[i]){
//				document.forms[0].year.options[i].selected=true;
//			}
//		}
//	} 
//	temp_clear();
//}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function temp_clear()
{
    ClearList(document.forms[0].staffyear,document.forms[0].staffyear);
    ClearList(document.forms[0].staffmaster,document.forms[0].staffmaster);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function OnAuthError()
{
	alert('{rval MZ0026}');
	closeWin();
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function LengthAdd(str,len,lenmax)
{
	bla = lenmax - len;
	if(bla > 0)
	{
		for (var h = 1; h <= bla; h++)
		{
			str = str + "　";
		}
		return str;
	}
}

//////////////////////////////////////////全追加、追加ボタン///////////////////////////////////////////////////////

function move(side)
{
	var temp1 = new Array();
	var temp2 = new Array();
	var tempa = new Array();
	var tempb = new Array();
	var current1 = 0;
	var current2 = 0;
	var y=0;
	var attribute;
	
	
	//年度データをストック
	for (var i = 0; i < document.forms[0].staffyear.length; i++)
	{  
		y=current1++;
		temp1[y] = document.forms[0].staffyear.options[i].value;
		tempa[y] = document.forms[0].staffyear.options[i].text;
	}

	//
	for (var i = 0; i < document.forms[0].staffmaster.length; i++)
	{
		mav = document.forms[0].staffmaster.options[i].value.split(',');
		mat = document.forms[0].staffmaster.options[i].text.split('|');

		switch(mav[1])
		{
			case "a":
				yearv = document.forms[0].staffmaster.options[i].value;
				yeart = "　追加　" + "|" + mat[1] + "|" + mat[2] + "|" + "　　　　　　" + "|" + "　　　　　　　　" + "|" + "　　　　　　　　" + "|" + "　" + "|" + "　　　　　　　　　　" + "|" + "　　　　　　　　　　";
				break;
			case "ud":
				yearv = mav[0] + "," + "u" + "," + mav[2] + "," + mav[3] + "," + mav[4] + "," + mav[5];
				yeart = "　更新　" + "|" + mat[1] + "|" + mat[2] + "|" + mat[3] + "|" + mat[4] + "|" + mat[5] + "|" + mat[6] + "|" + mat[7] + "|" + mat[8];
				break;
			case "d":
				yearv = mav[0] + "," + "" + "," + mav[2] + "," + mav[3] + "," + mav[4] + "," + mav[5];
				yeart = "　　　　" + "|" + mat[1] + "|" + mat[2] + "|" + mat[3] + "|" + mat[4] + "|" + mat[5] + "|" + mat[6] + "|" + mat[7] + "|" + mat[8];
		}

		if (side == "one")			//「追加」選択された分のみ年度データのストックに追加
		{
			if ( document.forms[0].staffmaster.options[i].selected )
			{
				y=current1++;
				temp1[y] = yearv;
				tempa[y] = yeart;
			}
			else
			{
				y=current2++;
				temp2[y] = document.forms[0].staffmaster.options[i].value;
				tempb[y] = document.forms[0].staffmaster.options[i].text;
			}
		}
		else						//「全追加」全部を年度データのストックに追加
		{

				y=current1++;
				temp1[y] = yearv;
				tempa[y] = yeart;
		}
	}

	//generating new options 
	ClearList(document.forms[0].staffyear,document.forms[0].staffyear);
	for (var i = 0; i < temp1.length; i++)
	{  
		document.forms[0].staffyear.options[i] = new Option();
		document.forms[0].staffyear.options[i].value = temp1[i];
		document.forms[0].staffyear.options[i].text =  tempa[i];
	}

	//generating new options
	ClearList(document.forms[0].staffmaster,document.forms[0].staffmaster);
	if (temp2.length>0)
	{	
		for (var i = 0; i < temp2.length; i++)
		{
			document.forms[0].staffmaster.options[i] = new Option();
			document.forms[0].staffmaster.options[i].value = temp2[i];
			document.forms[0].staffmaster.options[i].text =  tempb[i];
		}
	}

//	attribute3 = document.forms[0].selectdata;
//	attribute3.value = "";
//	sep = "";
//	for (var i = 0; i < document.forms[0].classyear.length; i++)
//	{
//		attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
//		sep = ",";
//	}

}


////////////////////////年度データを更新する内容を左側フレームに送る/////////////////////////////////////////////////
function ToListUpd()
{

	if(document.forms[0].JOBNAME.options[0])
	{
		var indj = document.forms[0].JOBNAME.selectedIndex;
		jov = document.forms[0].JOBNAME.options[indj].value;
		jot = document.forms[0].JOBNAME.options[indj].text;
		jotl = document.forms[0].JOBNAME.options[indj].text.length;
	}
	else
	{
		jov = "";
		jot = "";
		jotl = 0;
	}

	if(document.forms[0].SECTIONNAME.options[0])
	{
		var indsec = document.forms[0].SECTIONNAME.selectedIndex;
		sev = document.forms[0].SECTIONNAME.options[indsec].value;
		set = document.forms[0].SECTIONNAME.options[indsec].text;
		setl = document.forms[0].SECTIONNAME.options[indsec].text.length;
	}
	else
	{
		sev = "";
		set = "";
		setl = 0;
	}

	if(document.forms[0].SHARENAME.options[0])
	{
		var indsha = document.forms[0].SHARENAME.selectedIndex;
		shav = document.forms[0].SHARENAME.options[indsha].value;
		shat = document.forms[0].SHARENAME.options[indsha].text;
		shatl = document.forms[0].SHARENAME.options[indsha].text.length;
	}
	else
	{
		shav = "";
		shat = "";
		shatl = 0;
	}
	if(document.forms[0].HCLUBNAME.options[0])
	{
		var indhcl = document.forms[0].HCLUBNAME.selectedIndex;
		hclv = document.forms[0].HCLUBNAME.options[indhcl].value;
		hclt = document.forms[0].HCLUBNAME.options[indhcl].text;
		hcltl = document.forms[0].HCLUBNAME.options[indhcl].text.length;
	}
	else
	{
		hclv = "";
		hclt = "";
		hcltl = 0;
	}
	if(document.forms[0].SCLUBNAME.options[0])
	{
		var indscl = document.forms[0].SCLUBNAME.selectedIndex;
		sclv = document.forms[0].SCLUBNAME.options[indscl].value;
		sclt = document.forms[0].SCLUBNAME.options[indscl].text;
		scltl = document.forms[0].SCLUBNAME.options[indscl].text.length;
	}
	else
	{
		sclv = "";
		sclt = "";
		scltl = 0;
	}

	var indcla = document.forms[0].CHARGECLASS.selectedIndex;
	clav = "" + document.forms[0].CHARGECLASS.options[indcla].value;
	clat = document.forms[0].CHARGECLASS.options[indcla].text;

	parent.left_frame.FromEditUpd(jov,jot,jotl,sev,set,setl,shav,shat,shatl,hclv,hclt,hcltl,sclv,sclt,scltl,clav,clat);

}

//////////////////////////年度データを更新する内容を右側フレームから受け取る///////////////////////////////////////
function FromEditUpd(jovu,jotu,jotlu,sevu,setu,setlu,shavu,shatu,shatlu,hclvu,hcltu,hcltlu,sclvu,scltu,scltlu,clavu,clatu)
{
//	window.document.write(jotlu);
//	window.document.write(setlu);


	if(document.forms[0].staffyear.length == 0)
	{
		return;
	}
	else
	{
		flg1 = "";
		for(x = 0; x < document.forms[0].staffyear.length; x++)
		{
			if(document.forms[0].staffyear.options[x].selected == true)
			{
				flg1 = "ok";
			}
		}
		if(flg1 != "ok")
		{
			return;
		}
	}

		var inds = document.forms[0].staffyear.selectedIndex;
		var staval = document.forms[0].staffyear.options[inds].value.split(",");
		var statxt = document.forms[0].staffyear.options[inds].text.split("|");

		if(staval[1] == "a")				//更新データなのか追加データなのかのチェック
		{
			var markv = "a";
			var markt = "　追加　";
		}
		else
		{
			var markv = "u";
			var markt = "　更新　";
		}

		var njotu = LengthAdd(jotu,jotlu,6);
		var nsetu = LengthAdd(setu,setlu,8);
		var nshatu = LengthAdd(shatu,shatlu,8);
		var nhcltu = LengthAdd(hcltu,hcltlu,10);
		var nscltu = LengthAdd(scltu,scltlu,10);
	//	var nclatu = LengthAdd(clatu,8);

		var newval = staval[0] + "," + markv + "," + jovu + "," + sevu + "," + shavu + "," + clavu + "," + hclvu + "," + sclvu;
		var newtxt=markt + "|" + statxt[1] + "|" + statxt[2] + "|" + njotu + "|" + nsetu + "|" + nshatu + "|" + clatu + "|" + nhcltu + "|" + nscltu;

		document.forms[0].staffyear.options[inds] = new Option(newtxt,newval);

}

////////////////////////////////年度データから削除する内容を左側に渡す//////////////////////////////////////////
function ToListDel()
{
	parent.left_frame.FromEditDel();
}


///////////////////////////////右側から受け取ったデータを削除する//////////////////////////////////////////////////
function FromEditDel()
{
	var temp1 = new Array();
	var temp2 = new Array();
	var tempa = new Array();
	var tempb = new Array();
	var current1 = 0;
	var current2 = 0;
	var y=0;
	var attribute;
	
	
	//マスターデータをストック
	for (var i = 0; i < document.forms[0].staffmaster.length; i++)
	{
		y=current1++;
		temp1[y] = document.forms[0].staffmaster.options[i].value;
		tempa[y] = document.forms[0].staffmaster.options[i].text;
	}

	//
	xi = document.forms[0].staffyear.selectedIndex;			//年度データ選択されたデータのインデックス
	for (var i = 0; i < document.forms[0].staffyear.length; i++)
	{
		if(i == xi)
		{
			stav = document.forms[0].staffyear.options[i].value.split(",");
			stat = document.forms[0].staffyear.options[i].text.split("|");

			if(stav[1] == "a")				//追加データがマスターリストに戻されたとき
			{
				masv = stav[0] + stav[1];
				mast = "　　　　" + "|" + stat[1] + "|" + stat[2];
			}
			else							//すでにあるデータが削除されたとき
			{
				if(stav[1] == "u")					//一度更新したデータが削除された場合
				{
					mkt = "ud"
				}
				else
				{
					mkt = "d"						//読み込んだ状態のままで削除
				}
				masv = stav[0] + "," + mkt + "," + stav[2] + "," + stav[3] + "," + stav[4] + "," + stav[5] + "," + stav[6] + "," + stav[7];
				mast = "　削除　" + "|" + stat[1] + "|" + stat[2] + "|" + stat[3] + "|" + stat[4] + "|" + stat[5] + "|" + stat[6] + "|" + stat[7] + "|" + stat[8];
			}
			y=current1++;
			temp1[y] = masv;
			tempa[y] = mast;
		}
		else
		{
			y=current2++;
			temp2[y] = document.forms[0].staffyear.options[i].value;
			tempb[y] = document.forms[0].staffyear.options[i].text;
		}
	}

	//generating new options 
	ClearList(document.forms[0].staffmaster,document.forms[0].staffmaster);
	for (var i = 0; i < temp1.length; i++)
	{  
		document.forms[0].staffmaster.options[i] = new Option();
		document.forms[0].staffmaster.options[i].value = temp1[i];
		document.forms[0].staffmaster.options[i].text =  tempa[i];
	}

	//generating new options
	ClearList(document.forms[0].staffyear,document.forms[0].staffyear);
	if (temp2.length>0)
	{	
		for (var i = 0; i < temp2.length; i++)
		{
			document.forms[0].staffyear.options[i] = new Option();
			document.forms[0].staffyear.options[i].value = temp2[i];
			document.forms[0].staffyear.options[i].text =  tempb[i];
		}
	}

////	document.forms[0].staffyear.options[xi] = null;
////	document.forms[0].staffmaster.options[ml] = new Option (mast,masv);

}

////		yearfv = document.forms[0].staffyear.options[i].value.split(',');
////		yearft = document.forms[0].staffyear.options[i].text.split('|');
//////	ml = document.forms[0].staffmaster.length;				//マスタリストの長さ

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





//function ToList()
//{
//	var x = window.parent.left_frame.document.forms[0].staffmaster.selectedIndex;
//	stasele = window.parent.right_frame.document.forms[0].staffmaster;
//
//	stasele.options[x].value = document.forms[0].STAFFCD + "," + document.forms[0].SHARENAME.selected.value + "," + doc//ument.forms[0].JOBNAME.selected.value + "," + document.forms[0].SECTIONNAME.selected.value + "," + document.forms[0].//CHARGECLASS.selected.value;
//
//	stasele.options[x].text = document.forms[0].STAFFCD + "　" + document.forms[0].STAFFLNAME.value + "　" + document.f//orms[0].STAFFFNAME.value + "　" + document.forms[0].SHARENAME.selected.text + "　" + document.forms[0].JOBNAME.select//ed.text + "　" + document.forms[0].SECTIONNAME.selected.text + "　" + document.forms[0].CHARGECLASS.selected.text;
//
//}


