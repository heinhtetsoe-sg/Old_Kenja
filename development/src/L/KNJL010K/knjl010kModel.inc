<?php

require_once('for_php7.php');

class knjl010kModel extends Model {
    var $cmd;
    var $file;
    var $data_arr = array();
    var $controls = array();
    var $headercheck;
    var $testsubclasscd;
    var $testsubclassnm;
    var $coursecd;
    var $majorcd;
    var $coursecode;
    var $wk;
    var $appli;     //出願区分
    
    function knjl010kModel()
    {
        $this->year     = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->field["WHICH_WAY"] = VARS::post("WHICH_WAY");    //更新区分

        $this->cmd            = VARS::request("cmd");
        $temp                 = VARS::post("COURSEMAJOR");
        $this->course         = substr($temp,0,1);
        $this->major          = substr($temp,2,3);
        $this->testdiv        = VARS::post("TESTDIV");
        $this->testsubclass   = VARS::post("TESTSUBCLASS");
        $this->testsubclasscd = substr($this->testsubclass,0,1);
        $this->testsubclassnm = substr($this->testsubclass,2);

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
            $this->target      = "01";
        }else{
            $this->headercheck = VARS::post("HEADERCHECK");
            $this->target      = VARS::post("TARGET");
        }

        $this->file = VARS::file("FILE");
    }

    function &getUpdateModel()
    {
        //試験区分が空
        if($this->target == "01" || $this->target == "04" || $this->target == "06" ){
        }else{
            if($this->testdiv == "" || $this->testdiv == "null"){
                $this->setWarning("試験区分がありません。");
                return false;
            }
        }
        //事項時間の制限を無くす
        set_time_limit(0);

        $this->data_arr = array();
        //ファイルの文字列の取得
        $data_arr = $this->GetFileString();

        $errMsg = "";
        if ($this->target == "01") {
            $errMsg = $this->checkJizen($data_arr);
        }

        //データの整合性チェック
        if($this->IsDataMatched($data_arr) ) {
            if ($errMsg != "") {
                $this->setMessage("MSG203", "重複データを修正して下さい。\\n".$errMsg);
            } else {
                $cnt = knjl010kQuery::InsertQuery($this,$data_arr);
                switch($this->field["WHICH_WAY"]){
                    case 1:
                        $which_way = "追加";
                        break;
                    case 2:
                        $which_way = "更新";
                        break;
                    case 3:
                        $which_way = "削除";
                        break;
                }
                $this->setMessage("MSG202","読込み件数：".get_count($data_arr)."件\\n         ".$which_way."：".$cnt."件");
            }
        }
    }

    function checkJizen($data_arr)
    {
        $data_arr2 = $data_arr;

        $errMsg = "";
        $notData = array();
        $head = $this->headercheck == 1 ? 2 : 1;
        for ($cnt = 0; $cnt < get_count($data_arr); $cnt++) {
            if (in_array($cnt, $notData)) {
                continue;
            }
            for ($cnt2 = 0; $cnt2 < get_count($data_arr2); $cnt2++) {
                if ($cnt == $cnt2) {
                    continue;
                }
                if ($data_arr2[$cnt2]["TESTDIV"] == $data_arr[$cnt]["TESTDIV"] &&
                    $data_arr2[$cnt2]["NAME"] == $data_arr[$cnt]["NAME"] &&
                    $data_arr2[$cnt2]["NAME_KANA"] ==  $data_arr2[$cnt]["NAME_KANA"]
                ) {
                    $notData[] = $cnt2;
                    $errMsg .= ($cnt + $head)."行目と".($cnt2 + $head)."行目\\n";
                }
            }
        }

        return $errMsg;
    }

    function GetFileString()
    {
        $temp_arr = array();
        if(is_uploaded_file($this->file["tmp_name"])){
            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $header = false;
            while(!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if(trim($buffer) != '')
                {
                    if(!$header && $this->headercheck == 1){
                        $header = true;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    /* ---------------------------------------------------- */
                    /* 2005/10/19 minei                                     */
                    /* ＣＳＶに 対象データ、試験区分、出願区分 などを付加   */
                    /* ---------------------------------------------------- */
                    switch($this->target){
                        case "01"://事前相談データ
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "TESTDIV"         => $tmp[1],
                                          "CREATE_DATE"     => $tmp[2],
                                          "NAME"            => rtrim($tmp[3]),
                                          "NAME_KANA"       => rtrim($tmp[4]),
                                          "SEX"             => strtoupper($tmp[5]),
                                          "PS_UPDATED"      => $tmp[6],
                                          "PS_CD"           => (strlen(trim($tmp[7]))) ? sprintf("%07d",$tmp[7]) : "",
                                          "PS_ITEM1"        => $tmp[8],
                                          "PS_ITEM2"        => $tmp[9],    //2006.01.27 alp m-yama
                                          "PS_ITEM3"        => $tmp[10],
                                          "PS_ITEM4"        => $tmp[11],
                                          "PS_ITEM5"        => $tmp[12],
                                          "PORG_SHDIV1"     => strtoupper($tmp[13]),
                                          "PORG_MAJOR1"     => strtoupper($tmp[14]),
                                          "PORG_JUDGEMENT1" => strtoupper($tmp[15]),
                                          "PORG_SHDIV2"     => strtoupper($tmp[16]),
                                          "PORG_MAJOR2"     => strtoupper($tmp[17]),
                                          "PORG_JUDGEMENT2" => strtoupper($tmp[18]),
                                          "PORG_SHDIV3"     => strtoupper($tmp[19]),
                                          "PORG_MAJOR3"     => strtoupper($tmp[20]),
                                          "PORG_JUDGEMENT3" => strtoupper($tmp[21]),
                                          "PORG_SHDIV4"     => strtoupper($tmp[22]),
                                          "PORG_MAJOR4"     => strtoupper($tmp[23]),
                                          "PORG_JUDGEMENT4" => strtoupper($tmp[24]),
                                          "FS_UPDATED"      => $tmp[25],
                                          "FS_CD"           => (strlen(trim($tmp[26]))) ? sprintf("%07d",$tmp[26]) : "",
                                          "FS_ITEM1"        => $tmp[27],
                                          "FS_ITEM2"        => $tmp[28],
                                          "FS_ITEM3"        => $tmp[29],
                                          "FS_ITEM4"        => $tmp[30],
                                          "FS_ITEM5"        => $tmp[31],
                                          "FORG_SHDIV1"     => strtoupper($tmp[32]),
                                          "FORG_MAJOR1"     => strtoupper($tmp[33]),
                                          "FORG_JUDGEMENT1" => strtoupper($tmp[34]),
                                          "FORG_SHDIV2"     => strtoupper($tmp[35]),
                                          "FORG_MAJOR2"     => strtoupper($tmp[36]),
                                          "FORG_JUDGEMENT2" => strtoupper($tmp[37]),
                                          "FORG_SHDIV3"     => strtoupper($tmp[38]),
                                          "FORG_MAJOR3"     => strtoupper($tmp[39]),
                                          "FORG_JUDGEMENT3" => strtoupper($tmp[40]),
                                          "FORG_SHDIV4"     => strtoupper($tmp[41]),
                                          "FORG_MAJOR4"     => strtoupper($tmp[42]),
                                          "FORG_JUDGEMENT4" => strtoupper($tmp[43]));
                            $temp_arr[$i] = $data;
                            break;
                        case "02"://志願者（速報）データ
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "EXAMNO"          => (strlen(trim($tmp[1]))) ? sprintf("%04d",$tmp[1]) : "",
                                          "RECEPT_DATE"     => $tmp[2],
                                          "SHDIV"           => strtoupper($tmp[3]),
                                          "DESIREDIV"       => (strlen(trim($tmp[4]))) ? sprintf("%02d",$tmp[4]) : "",
                                          "SEX"             => strtoupper($tmp[5]),
                                          "NATPUBPRIDIV"    => $tmp[6]);
                            $temp_arr[$i] = $data;
                            break;
                        case "03"://志願者（基礎）データ
                            //2005/12/29 by OCC
                            $data = array(
                                          "TARGET_TABLE"       => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "TESTDIV"            => $tmp[1],
                                          "APPLICANTDIV"       => $tmp[2],
                                          "EXAMNO"             => (strlen(trim($tmp[3]))) ? sprintf("%04d",$tmp[3]) : "",
                                          "RECEPT_DATE"        => $tmp[4],
                                          "SHDIV"              => strtoupper($tmp[5]),
                                          "DESIREDIV"          => (strlen(trim($tmp[6]))) ? sprintf("%02d",$tmp[6]) : "",
                                          "SPECIAL_REASON_DIV" => strlen($tmp[7]) ? $tmp[7] : 0,
                                          "NAME"               => rtrim($tmp[8]),
                                          "NAME_KANA"          => rtrim($tmp[9]),
                                          "SEX"                => strtoupper($tmp[10]),
                                          "BIRTHDAY"           => $tmp[11],
                                          "ADDRESSCD"          => (strlen(trim($tmp[12]))) ? sprintf("%02d",$tmp[12]) : "",
                                          "TELNO"              => $tmp[13],
                                          "LOCATIONCD"         => (strlen(trim($tmp[14]))) ? sprintf("%02d",$tmp[14]) : "",
                                          "NATPUBPRIDIV"       => $tmp[15],
                                          "FS_CD"              => (strlen(trim($tmp[16]))) ? sprintf("%07d",$tmp[16]) : "",
                                          "FS_GRDYEAR"         => $tmp[17],
                                          "FS_HRCLASS"         => $tmp[18],
                                          "FS_ATTENDNO"        => $tmp[19],
                                          "PS_CD"              => (strlen(trim($tmp[20]))) ? sprintf("%07d",$tmp[20]) : "",
                                          "APPROVAL_FLG"       => $tmp[21],        #2005/11/08 arakaki
                                          "GNAME"              => $tmp[22],
                                          "GKANA"              => $tmp[23],
                                          "GTELNO"             => $tmp[24],
                                          "FORMNO"             => $tmp[25]);

                               $temp_arr[$i] = $data;
                            break;
                        case "04"://志願者（住所）データ
                            //2005/12/30 by OCC
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "TESTDIV"         => $tmp[1],
                                          "EXAMNO"          => (strlen(trim($tmp[2]))) ? sprintf("%04d",$tmp[2]) : "",
                                          "ZIPCD"           => $tmp[3],
                                          "ADDRESS"         => $tmp[4],
                                          "GZIPCD"          => $tmp[5],
                                          "GADDRESS"        => $tmp[6]);
                            $temp_arr[$i] = $data;
                            break;
                        case "05"://志願者（内申）データ
                            $data = array(
                                          "TARGET_TABLE"       => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "EXAMNO"             => (strlen(trim($tmp[1]))) ? sprintf("%04d",$tmp[1]) : "",
                                          "CONFIDENTIAL_RPT1"  => $tmp[2],
                                          "CONFIDENTIAL_RPT2"  => $tmp[3],
                                          "CONFIDENTIAL_RPT3"  => $tmp[4],
                                          "CONFIDENTIAL_RPT4"  => $tmp[5],
                                          "CONFIDENTIAL_RPT5"  => $tmp[6],
                                          "CONFIDENTIAL_RPT6"  => $tmp[7],
                                          "CONFIDENTIAL_RPT7"  => $tmp[8],
                                          "CONFIDENTIAL_RPT8"  => $tmp[9],
                                          "CONFIDENTIAL_RPT9"  => $tmp[10],
                                          "CONFIDENTIAL_RPT10" => $tmp[11],
                                          "CONFIDENTIAL_RPT11" => $tmp[12],
                                          "CONFIDENTIAL_RPT12" => $tmp[13],
                                          "TOTAL_REPORT"       => $tmp[14],
                                          "AVERAGE5"           => $tmp[15],
                                          "AVARAGE_ALL"        => $tmp[16]);
                            $temp_arr[$i] = $data;
                            break;
                        case "06"://志願者（附属推薦または中高一貫）
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "TESTDIV"         => $tmp[1],
                                          "APPLICANTDIV"    => $tmp[2],
                                          "EXAMNO"          => (strlen(trim($tmp[3]))) ? sprintf("%04d",$tmp[3]) : "",
                                          "RECEPT_DATE"     => $tmp[4],
                                          "SHDIV"           => strtoupper($tmp[5]),
                                          "DESIREDIV"       => (strlen(trim($tmp[6]))) ? sprintf("%02d",$tmp[6]) : "",
                                          "SPECIAL_REASON_DIV" => strlen($tmp[7]) ? $tmp[7] : 0,
                                          "NAME"            => rtrim($tmp[8]),
                                          "NAME_KANA"       => rtrim($tmp[9]),
                                          "SEX"             => strtoupper($tmp[10]),
                                          "BIRTHDAY"        => $tmp[11],
                                          "ADDRESSCD"       => (strlen(trim($tmp[12]))) ? sprintf("%02d",$tmp[12]) : "",
                                          "TELNO"           => $tmp[13],
                                          "LOCATIONCD"      => (strlen(trim($tmp[14]))) ? sprintf("%02d",$tmp[14]) : "",
                                          "NATPUBPRIDIV"    => $tmp[15],
                                          "FS_CD"           => (strlen(trim($tmp[16]))) ? sprintf("%07d",$tmp[16]) : "",
                                          "FS_GRDYEAR"      => $tmp[17],
                                          "FS_HRCLASS"      => $tmp[18],
                                          "FS_ATTENDNO"     => $tmp[19],
                                          "PS_CD"           => (strlen(trim($tmp[20]))) ? sprintf("%07d",$tmp[20]) : "",
                                          "APPROVAL_FLG"    => $tmp[21],        #2005/11/08 arakaki
                                          "GNAME"           => $tmp[22],
                                          "GKANA"           => $tmp[23],
                                          "GTELNO"          => $tmp[24],
                                          "FORMNO"          => $tmp[25]);
                            $temp_arr[$i] = $data;
                            break;
                        case "07"://クラブ推薦者データ
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "DIV"             => $tmp[1],
                                          "EXAMNO"          => (strlen(trim($tmp[2]))) ? sprintf("%04d",$tmp[2]) : "",
                                          "CLUBCD"          => (strlen(trim($tmp[3]))) ? sprintf("%04d",$tmp[3]) : "");    //2006.01.17 alp m-yama
                            $temp_arr[$i] = $data;
                            break;
                        case "08"://得点データ
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "TESTDIV"         => $tmp[1],
                                          "EXAMNO"          => (strlen(trim($tmp[2]))) ? sprintf("%04d",$tmp[2]) : "",
                                          "TESTSUBCLASSCD"  => (strlen(trim($tmp[3]))) ? sprintf("%01d",$tmp[3]) : "",
                                          "A_SCORE"         => $tmp[4],
                                          "B_SCORE"         => $tmp[5]);
                            $temp_arr[$i] = $data;
                            break;
                        case "09"://スカラ対象データ
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "EXAMNO"          => (strlen(trim($tmp[1]))) ? sprintf("%04d",$tmp[1]) : "",
                                          "SCALASHIPDIV"    => $tmp[2]);
                            $temp_arr[$i] = $data;
                            break;
                        case "10":    //受験者出身塾データ  2005/10/19 minei
                            $data = array(
                                          "TARGET_TABLE"    => (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "",
                                          "EXAMNO"          => (strlen(trim($tmp[1]))) ? sprintf("%04d",$tmp[1]) : "",
                                          "PS_CD"           => $tmp[2]);
                            $temp_arr[$i] = $data;
                            break;
                    }
                    $i++;
                }
            }
            fclose($fp);

        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $temp_arr;
    }

    /* ---------------------------- */
    /* データの整合性チェック       */
    /* ---------------------------- */
    function IsDataMatched(&$data_arr)
    {
        $db = Query::dbCheckOut();
        //塾-----------------------2005/08/09
        $this->pshdivarr = array();
        $this->pjudgearr = array();
        $this->porgarr   = array();
        //出身学校-----------------2005/08/09
        $this->fshdivarr = array();
        $this->fjudgearr = array();
        $this->forgarr   = array();

        switch($this->target){
            case "01"://事前相談
                $data_each = array(
                                   "TERGET_TBL"      => array(2,"対象データ"),
                                   "TESTDIV"         => array(1,"試験区分"),
                                   "CREATE_DATE"     => array(10,"作成日付"),
                                   "NAME"            => array(63,"氏名"),
                                   "NAME_KANA"       => array(243,"氏名かな"),
                                   "SEX"             => array(1,"性別コード"),
                                   "PS_UPDATED"      => array(10,"登録日付"),
                                   "PS_CD"           => array(7,"塾コード"),
                                   "PS_ITEM1"        => array(3,"実力テスト得点"),
                                   "PS_ITEM2"        => array(5,"実力テスト平均"),
                                   "PS_ITEM3"        => array(5,"得点－平均"),
                                   "PS_ITEM4"        => array(5,"五ツ木偏差値"),
                                   "PS_ITEM5"        => array(5,"各種模擬偏差値"),
                                   "PORG_SHDIV1"     => array(1,"専併区分１"),
                                   "PORG_MAJOR1"     => array(1,"学科１"),
                                   "PORG_JUDGEMENT1" => array(1,"判定１"),
                                   "PORG_SHDIV2"     => array(1,"専併区分２"),
                                   "PORG_MAJOR2"     => array(1,"学科２"),
                                   "PORG_JUDGEMENT2" => array(1,"判定２"),
                                   "PORG_SHDIV3"     => array(1,"専併区分３"),
                                   "PORG_MAJOR3"     => array(1,"学科３"),
                                   "PORG_JUDGEMENT3" => array(1,"判定３"),
                                   "PORG_SHDIV4"     => array(1,"専併区分４"),
                                   "PORG_MAJOR4"     => array(1,"学科４"),
                                   "PORG_JUDGEMENT4" => array(1,"判定４"),
                                   "FS_UPDATED"      => array(10,"登録日付"),
                                   "FS_CD"           => array(7,"学校コード"),
                                   "FS_ITEM1"        => array(3,"実力テスト得点"),
                                   "FS_ITEM2"        => array(5,"実力テスト平均"),
                                   "FS_ITEM3"        => array(5,"得点－平均"),
                                   "FS_ITEM4"        => array(5,"五ツ木偏差値"),
                                   "FS_ITEM5"        => array(5,"各種模擬偏差値"),
                                   "FORG_SHDIV1"     => array(1,"専併区分１"),
                                   "FORG_MAJOR1"     => array(1,"学科１"),
                                   "FORG_JUDGEMENT1" => array(1,"判定１"),
                                   "FORG_SHDIV2"     => array(1,"専併区分２"),
                                   "FORG_MAJOR2"     => array(1,"学科２"),
                                   "FORG_JUDGEMENT2" => array(1,"判定２"),
                                   "FORG_SHDIV3"     => array(1,"専併区分３"),
                                   "FORG_MAJOR3"     => array(1,"学科３"),
                                   "FORG_JUDGEMENT3" => array(1,"判定３"),
                                   "FORG_SHDIV4"     => array(1,"専併区分４"),
                                   "FORG_MAJOR4"     => array(1,"学科４"),
                                   "FORG_JUDGEMENT4" => array(1,"判定４") );
                break;
            case "02"://志願者（速報）データ
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "RECEPT_DATE"    => array(10,"受付日付"),
                                   "SHDIV"          => array(1,"専併区分"),
                                   "DESIREDIV"      => array(2,"志望区分"),
                                   "SEX"            => array(1,"性別コード"),
                                   "NATPUBPRIDIV"   => array(1,"国公私立区分コード") );
                break;
            case "03"://志願者（基礎）データ
                $data_each = array(
                                   "TERGET_TBL"         => array(2,"対象データ"),
                                   "TESTDIV"            => array(1,"試験区分"),     #ADD 2005/12/29 by OCC
                                   "APPLICANTDIV"       => array(1,"出願区分"),     #ADD 2005/12/30 by OCC minei
                                   "EXAMNO"             => array(4,"受験番号"),
                                   "RECEPT_DATE"        => array(10,"受付日付"),
                                   "SHDIV"              => array(1,"専併区分"),
                                   "DESIREDIV"          => array(2,"志望区分"),
                                   "SPECIAL_REASON_DIV" => array(1,"特別理由区分"),
                                   "NAME"               => array(60,"氏名"),
                                   "NAME_KANA"          => array(120,"氏名かな"),
                                   "SEX"                => array(1,"性別コード"),
                                   "BIRTHDAY"           => array(10,"生年月日"),
                                   "ADDRESSCD"          => array(2,"現住所コード"),
                                   "TELNO"              => array(14,"電話番号"),    //2006.01.25 alp m-yama
                                   "LOCATIONCD"         => array(2,"出身学校所在地コード"),
                                   "NATPUBPRIDIV"       => array(1,"国公私立区分コード"),
                                   "FS_CD"              => array(7,"出身学校コード"),
                                   "FS_GRDYEAR"         => array(4,"卒業年"),
                                   "FS_HRCLASS"         => array(3,"出身学校組"),
                                   "FS_ATTENDNO"        => array(3,"出身学校出席番号"),
                                   "PS_CD"              => array(7,"塾コード"),
                                   "APPROVAL_FLG"       => array(1,"通知承諾フラグ"),   #2005/11/08 arakaki
                                   "GNAME"              => array(60,"保護者氏名"),
                                   "GKANA"              => array(120,"保護者かな"),
                                   "GTELNO"             => array(14,"保護者電話番号"),    //2006.01.25 alp m-yama
                                   "FORMNO"             => array(6,"フォームNo.") );
                break;
            case "04"://志願者（住所）データ
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "TESTDIV"        => array(1,"試験区分"),        //2005/12/30 by OCC
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "ZIPCD"          => array(8,"郵便番号"),
                                   "ADDRESS"        => array(153,"住所"),
                                   "GZIPCD"         => array(8,"保護者郵便番号"),
                                   "GADDRESS"       => array(153,"保護者住所") );
                break;
            case "05"://志願者（内申）データ
                $data_each = array(
                                   "TERGET_TBL"         => array(2,"対象データ"),
                                   "EXAMNO"             => array(4,"受験番号"),
                                   "CONFIDENTIAL_RPT1"  => array(2,"教科１"),
                                   "CONFIDENTIAL_RPT2"  => array(2,"教科２"),
                                   "CONFIDENTIAL_RPT3"  => array(2,"教科３"),
                                   "CONFIDENTIAL_RPT4"  => array(2,"教科４"),
                                   "CONFIDENTIAL_RPT5"  => array(2,"教科５"),
                                   "CONFIDENTIAL_RPT6"  => array(2,"教科６"),
                                   "CONFIDENTIAL_RPT7"  => array(2,"教科７"),
                                   "CONFIDENTIAL_RPT8"  => array(2,"教科８"),
                                   "CONFIDENTIAL_RPT9"  => array(2,"教科９"),
                                   "CONFIDENTIAL_RPT10" => array(2,"教科１０"),
                                   "CONFIDENTIAL_RPT11" => array(2,"教科１１"),
                                   "CONFIDENTIAL_RPT12" => array(2,"教科１２"),
                                   "TOTAL_REPORT"       => array(4,"総評"),
                                   "AVERAGE5"           => array(4,"５教科平均"),
                                   "AVARAGE_ALL"        => array(4,"全教科平均") );
                break;
            case "06"://志願者（附属推薦または中高一貫）データ
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "TESTDIV"        => array(1,"試験区分"),
                                   "APPLICANTDIV"   => array(1,"出願区分"),     #ADD 2005/12/30 by OCC minei
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "RECEPT_DATE"    => array(10,"受付日付"),
                                   "SHDIV"          => array(1,"専併区分"),
                                   "DESIREDIV"      => array(2,"志望区分"),
                                   "SPECIAL_REASON_DIV" => array(1,"特別理由区分"),
                                   "NAME"           => array(60,"氏名"),
                                   "NAME_KANA"      => array(120,"氏名かな"),
                                   "SEX"            => array(1,"性別コード"),
                                   "BIRTHDAY"       => array(10,"生年月日"),
                                   "ADDRESSCD"      => array(2,"現住所コード"),
                                   "TELNO"          => array(14,"電話番号"),    //2006.01.25 alp m-yama
                                   "LOCATIONCD"     => array(2,"出身学校所在地コード"),
                                   "NATPUBPRIDIV"   => array(1,"国公私立区分コード"),
                                   "FS_CD"          => array(7,"出身学校コード"),
                                   "FS_GRDYEAR"     => array(4,"卒業年"),
                                   "FS_HRCLASS"     => array(3,"出身学校組"),
                                   "FS_ATTENDNO"    => array(3,"出身学校出席番号"),
                                   "PS_CD"          => array(7,"塾コード"),
                                   "APPROVAL_FLG"   => array(1,"通知承諾フラグ"),   #2005/11/08 arakaki
                                   "GNAME"          => array(60,"保護者氏名"),
                                   "GKANA"          => array(120,"保護者かな"),
                                   "GTELNO"         => array(14,"保護者電話番号"),    //2006.01.25 alp m-yama
                                   "FORMNO"         => array(6,"フォームNo.") );
                break;
            case "07"://クラブ推薦者データ
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "DIV"            => array(1,"区分"),
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "CLUBCD"         => array(4,"クラブコード") );    //2006.01.17 alp m-yama
                break;
            case "08"://得点データ
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "TESTDIV"        => array(1,"試験区分"),
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "TESTSUBCLASSCD" => array(1,"試験科目コード"),
                                   "A_SCORE"        => array(3,"Ａ得点"),
                                   "B_SCORE"        => array(3,"Ｂ得点") );
                break;
            case "09"://スカラ対象データ
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "SCALASHIPDIV"   => array(2,"スカラシップ区分") );
                break;
            case "10"://出身塾データ  2005/10/19 minei
                $data_each = array(
                                   "TERGET_TBL"     => array(2,"対象データ"),
                                   "EXAMNO"         => array(4,"受験番号"),
                                   "PS_CD"          => array(7,"塾コード") );
                break;
        }
        //中学か高校か判断
        $junior = false;
        if (knjl010kQuery::getSchoolName($db) > 0){
            $junior = true;
        }
        $count = get_count($data_arr);    //←事前相談もヘッダが１行に変更 2005/08/10 
        for($i=0; $i<$count; $i++){
            foreach($data_each as $key => $val){

                switch($this->target){
                    case "01"://事前相談データ
                        if(in_array($key,array("TARGET_TABLE","TESTDIV","CREATE_DATE","NAME"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","氏名：".$data_arr[$i]["NAME"]."\\n項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "02"://志願者（速報）データ
                        if(in_array($key,array("TARGET_TABLE","EXAMNO","RECEPT_DATE","SHDIV","DESIREDIV","SEX","NATPUBPRIDIV"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "03": //志願者(基礎)データ minei 2005/10/19  チェックは受験番号と漢字氏名のみ！+試験区分、出願区分
                        if(in_array($key,array("TARGET_TABLE","TESTDIV","APPLICANTDIV","EXAMNO","NAME"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "04"://志願者（住所）データ
                        if(in_array($key,array("TARGET_TABLE","TESTDIV","EXAMNO"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "05"://志願者（内申）データ
                        if(in_array($key,array("TARGET_TABLE","EXAMNO"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "06"://志願者（附属推薦または中高一貫）データ
                        if(in_array($key,array("TARGET_TABLE","TESTDIV","APPLICANTDIV","EXAMNO","DESIREDIV","NAME"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "07"://クラブ推薦者データ
                        if(in_array($key,array("TARGET_TABLE","DIV","EXAMNO"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "08"://得点データ
                        if(in_array($key,array("TARGET_TABLE","TESTDIV","EXAMNO","TESTSUBCLASSCD"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "09"://スカラ対象データ
                        if(in_array($key,array("TARGET_TABLE","EXAMNO","SCALASHIPDIV"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                    case "10"://出身塾データ
                        if(in_array($key,array("TARGET_TABLE","EXAMNO","PS_CD"))){
                            if(!strlen(trim($data_arr[$i][$key]))){
                                $this->setWarning("MSG301","受験番号：".$data_arr[$i]["EXAMNO"]."\\n      項目：".$val[1]);
                                return false;
                            }
                        }
                        break 1;
                }

                //文字数チェック
                if(strlen($data_arr[$i][$key]) > $val[0]){
                    if($this->target == "01"){
                        $this->setWarning("MSG915",
                                      "( ".$val[1]." )".$val[0]."バイトまで 名前：".$data_arr[$i]["NAME"]." 値：".$data_arr[$i][$key]);
                    }else{
                        $this->setWarning("MSG915",
                                      "( ".$val[1]." )".$val[0]."バイトまで 受験番号：".$data_arr[$i]["EXAMNO"]." 値：".$data_arr[$i][$key]);
                    }
                    return false;
                }
            }

            //対象データ 2005/10/19 minei
            if($data_arr[$i]["TARGET_TABLE"] != $this->target){
                if($this->target == "01"){
                    $this->setWarning("MSG913","( 対象データ )  \\n氏名：".$data_arr[$i]["NAME"]);
                }else {
                    $this->setWarning("MSG913","( 対象データ )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                }
                return false;
                
            }
            //試験区分(対象データ 1:事前相談, 4:住所, 6:附属推薦中高一貫) 2005/12/30 minei
            if($this->target == "01" || $this->target == "04" || $this->target == "06"){
                if($this->testdiv != "" && $data_arr[$i]["TESTDIV"] != $this->testdiv){
                    $this->setWarning("MSG913","( 試験区分 )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }else if($this->target == "03" || $this->target == "08"){
                if($data_arr[$i]["TESTDIV"] != $this->testdiv){
                    $this->setWarning("MSG913","( 試験区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
            }

            //試験区分(対象データ 3:志願者基礎データ)
            if($this->target == "03"){
                //0:一般以外はエラー
                if($data_arr[$i]["APPLICANTDIV"] != 0 ){
                    $this->setWarning("MSG913","( 出願区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
                //通知承諾フラグ(approval_flg)はnull,1,2以外は不正
                if($data_arr[$i]["APPROVAL_FLG"] != "" && $data_arr[$i]["APPROVAL_FLG"] != 1 && $data_arr[$i]["APPROVAL_FLG"] != 2 ){
                    $this->setWarning("MSG913","( 通知承諾フラグ )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L003",$data_arr[$i]["TESTDIV"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 試験区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L017",$data_arr[$i]["SPECIAL_REASON_DIV"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 特別理由区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
            }

            //試験区分(対象データ 6:附属推薦中高一貫) 2005/10/19 minei
            if($this->target == "06"){
                //UPDATE 2005/12/29 by OCC
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L003",$data_arr[$i]["TESTDIV"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 試験区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
                //1:付属推薦(中学のみ）、２：中高一貫（高校のみ）以外はエラー
                if ($junior && $data_arr[$i]["APPLICANTDIV"] == 1){
                }else if (!$junior && $data_arr[$i]["APPLICANTDIV"] == 2){
                }else{
                    $this->setWarning("MSG913","( 出願区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
                //ADD 2005/12/29 by OCC
                //通知承諾フラグ(approval_flg)はnull,1,2以外は不正
                if($data_arr[$i]["APPROVAL_FLG"] != "" && $data_arr[$i]["APPROVAL_FLG"] != 1 && $data_arr[$i]["APPROVAL_FLG"] != 2 ){
                    $this->setWarning("MSG913","( 通知承諾フラグ )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
                //志望区分
                $r = knjl010kQuery::GetDesirediv($db,$this->year,$data_arr[$i]["TESTDIV"],$data_arr[$i]["DESIREDIV"]);
                if (!is_array($r)){
                    $this->setWarning("MSG913","( 志望区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }else{
                    $data_arr[$i]["SUC_COURSECD"]     = $r["COURSECD"];
                    $data_arr[$i]["SUC_MAJORCD"]      = $r["MAJORCD"];
                    $data_arr[$i]["SUC_COURSECODE"]   = $r["EXAMCOURSECD"];
                }
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L017",$data_arr[$i]["SPECIAL_REASON_DIV"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 特別理由区分 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
            }
            //試験区分(対象データ 8:得点) 2005/10/19 minei
            if($this->target == "08"){
                if($data_arr[$i]["TESTSUBCLASSCD"] != $this->testsubclasscd){
                    $this->setWarning("MSG913","( 受験科目 )  \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }
            }



            //性別コード
            if(strlen(trim($data_arr[$i]["SEX"]))!=0){
                 $wk=knjl010kQuery::GetNamecd($db,$this->year,"Z002",$data_arr[$i]["SEX"]);
                if(!isset($wk)){
                    if($this->target == "01"){
                        $this->setWarning("MSG913","( 性別コード )  \\n氏名：".$data_arr[$i]["NAME"]);
                        return false;
                    }else{
                        $this->setWarning("MSG913","( 性別コード )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                        return false;
                    }
                }
            }

            //専併区分、学科、判定  チェック(1：事前相談データのみ)
            if($this->target == "01"){
                //塾------------------------------------------------------------
                for($psh=1; $psh<5; $psh++){
                       //専併区分
                       if(strlen(trim($data_arr[$i]["PORG_SHDIV".$psh]))!=0){
                            //UPDATE 2005/12/30 by OCC
                            $wk = knjl010kQuery::getOne($db,"select namecd2 from v_name_mst where year = '".$this->year."' and namecd1 ='L006' and abbv1 = '".$data_arr[$i]["PORG_SHDIV".$psh]."'");
                           if(!isset($wk)){//略称１で比較してなければエラー
                               $this->setWarning("MSG913","( 塾：専併区分".$psh." )  \\n氏名：".$data_arr[$i]["NAME"]);
                               return false;
                           }else{
                               $this->pshdivarr[$i][$psh] = $wk;
                           }
                       }else{
                           $this->pshdivarr[$i][$psh] = "";
                       }
                       //学科
                       if(strlen(trim($data_arr[$i]["PORG_MAJOR".$psh]))!=0){
                           
                            //UPDATE 2005/12/30 by OCC
                           $wk = knjl010kQuery::getOne($db,"SELECT coursecd || majorcd || examcoursecd FROM entexam_course_mst WHERE entexamyear = '".$this->year."' and examcourse_mark = '".$data_arr[$i]["PORG_MAJOR".$psh]."'");
                           if(!isset($wk)){//コース記号で比較してなければエラー
                               $this->setWarning("MSG913","( 塾：学科".$psh." )  \\n氏名：".$data_arr[$i]["NAME"]);
                               return false;
                           }else{
                               $this->porgarr[$i][$psh] = $wk;
                           }
                       }else{
                           $this->porgarr[$i][$psh] = "";
                       }
                       //判定
                       if(strlen(trim($data_arr[$i]["PORG_JUDGEMENT".$psh]))!=0){
                            //UPDATE 2005/12/30 by OCC
                           $wk = knjl010kQuery::getOne($db,"select namecd2 from v_name_mst where year = '".$this->year."' and namecd1 ='L002' and name1 = '".$data_arr[$i]["PORG_JUDGEMENT".$psh]."'");
                           if(!isset($wk)){//名称１で比較してなければエラー
                               $this->setWarning("MSG913","( 塾：判定".$psh." )  \\n氏名：".$data_arr[$i]["NAME"]);
                               return false;
                           }else{
                               $this->pjudgearr[$i][$psh] = $wk;
                           }
                       }else{
                           $this->pjudgearr[$i][$psh] = "";
                       }

                }

                //出身学校------------------------------------------------------------
                for($fsh=1; $fsh<5; $fsh++){
                        //専併区分
                        if(strlen(trim($data_arr[$i]["FORG_SHDIV".$fsh]))!=0){
                            //UPDATE 2005/12/30 by OCC
                            $wk = knjl010kQuery::getOne($db,"select namecd2 from v_name_mst where year = '".$this->year."' and namecd1 ='L006' and abbv1 = '".$data_arr[$i]["FORG_SHDIV".$fsh]."'");
                            if(!isset($wk)){//略称１で比較してなければエラー
                                $this->setWarning("MSG913","( 出身学校：専併区分".$fsh." )  \\n氏名：".$data_arr[$i]["NAME"]);
                                return false;
                            }else{
                                $this->fshdivarr[$i][$fsh] = $wk;
                            }
                        }else{
                            $this->fshdivarr[$i][$fsh] = "";
                        }
                        //学科
                        if(strlen(trim($data_arr[$i]["FORG_MAJOR".$fsh]))!=0){
                            //UPDATE 2005/12/30 by OCC
                            $wk = knjl010kQuery::getOne($db,"SELECT coursecd || majorcd || examcoursecd FROM entexam_course_mst WHERE entexamyear = '".$this->year."' and examcourse_mark = '".$data_arr[$i]["FORG_MAJOR".$fsh]."'");
                            if(!isset($wk)){//コース記号で比較してなければエラー
                                $this->setWarning("MSG913","( 出身学校：学科".$fsh." )  \\n氏名：".$data_arr[$i]["NAME"]);
                                return false;
                            }else{
                                $this->forgarr[$i][$fsh] = $wk;
                            }
                        }else{
                            $this->forgarr[$i][$fsh] = "";
                        }
                        //判定
                        if(strlen(trim($data_arr[$i]["FORG_JUDGEMENT".$fsh]))!=0){
                            //UPDATE 2005/12/30 by OCC
                            $wk = knjl010kQuery::getOne($db,"select namecd2 from v_name_mst where year = '".$this->year."' and namecd1 ='L002' and name1 = '".$data_arr[$i]["FORG_JUDGEMENT".$fsh]."'");
                            if(!isset($wk)){//名称１で比較してなければエラー
                                return false;
                            }else{
                                $this->fjudgearr[$i][$fsh] = $wk;
                            }
                        }else{
                            $this->fjudgearr[$i][$fsh] = "";
                        }
                }

            }else{
                if(strlen(trim($data_arr[$i]["SHDIV"]))!=0){
                    //UPDATE 2005/12/30 by OCC
                    $wk = knjl010kQuery::getOne($db,"select namecd2 from v_name_mst where year = '".$this->year."' and namecd1 ='L006' and abbv1 = '".$data_arr[$i]["SHDIV"]."'");
                    if(!isset($wk)){//略称１で比較してなければエラー
                        $this->setWarning("MSG913","( 専併区分 )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                        return false;
                    }
                }
            }


            //志望区分
            if(strlen(trim($data_arr[$i]["DESIREDIV"]))!=0){

                if($this->target == 4 || $this->target == 6){ #2006/01/06
                    $wk = knjl010kQuery::getOne($db,"SELECT distinct(desirediv) AS desirediv FROM entexam_wishdiv_mst WHERE entexamyear = '".$this->year."' AND testdiv = '".$data_arr[$i]["TESTDIV"]."' AND desirediv = '".$data_arr[$i]["DESIREDIV"]."' ");
                }else{
                    $wk = knjl010kQuery::getOne($db,"SELECT distinct(desirediv) AS desirediv FROM entexam_wishdiv_mst WHERE entexamyear = '".$this->year."' AND testdiv = '".$this->testdiv."' AND desirediv = '".$data_arr[$i]["DESIREDIV"]."' ");
                }
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 志望区分 )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            //国公私立区分コード
            if(strlen(trim($data_arr[$i]["NATPUBPRIDIV"]))!=0){
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L004",$data_arr[$i]["NATPUBPRIDIV"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 国公私立区分コード )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            //現住所コード
            if(strlen(trim($data_arr[$i]["ADDRESSCD"]))!=0){
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L007",$data_arr[$i]["ADDRESSCD"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 現住所コード )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            //出身学校所在地コード
            if(strlen(trim($data_arr[$i]["LOCATIONCD"]))!=0){
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"L007",$data_arr[$i]["LOCATIONCD"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 出身学校所在地コード )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            //卒業年
            if(strlen(trim($data_arr[$i]["FS_GRDYEAR"]))!=0 & strlen(trim($data_arr[$i]["FS_GRDYEAR"]))!=4){
                $this->setWarning("MSG913","( 卒業年 )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            //クラブ推薦区分
            if(strlen(trim($data_arr[$i]["DIV"]))!=0 & $data_arr[$i]["DIV"]!=1){
                $this->setWarning("MSG901","( 区分は１です )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            //受験科目の一致(ＣＳＶファイルと画面)
            if($this->testsubclasscd != $data_arr[$i]["TESTSUBCLASSCD"]){
                $this->setWarning("対象ファイルと画面の受験科目が違います。( 試験科目コード ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            //スカラコード
            if(strlen(trim($data_arr[$i]["SCALASHIPDIV"]))!=0){
                $wk=knjl010kQuery::GetNamecd($db,$this->year,"Z006",$data_arr[$i]["SCALASHIPDIV"]);
                if(!isset($wk)){
                    $this->setWarning("MSG913","( 対象スカラコード )  \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            //日付型チェック
            if(is_numeric($data_arr[$i]["RECEPT_DATE"])){
                $date = $data_arr[$i]["RECEPT_DATE"];
                if(strlen($date) == 8){
                    $data_arr[$i]["RECEPT_DATE"] = substr($date,0,4)."-".substr($date,4,2)."-".substr($date,6,2);
                }else{
                    $this->setWarning("日付の書式が不正です( 受付日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }elseif ($data_arr[$i]["RECEPT_DATE"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/",$data_arr[$i]["RECEPT_DATE"])){
                $this->setWarning("日付の書式が不正です( 受付日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if($this->IsDate($data_arr[$i]["RECEPT_DATE"]) == false){
               $this->setWarning("日付が不正です( 受付日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
               return false;
            }
            if(is_numeric($data_arr[$i]["BIRTHDAY"])){
                $date = $data_arr[$i]["BIRTHDAY"];
                if(strlen($date) == 8){
                    $data_arr[$i]["BIRTHDAY"] = substr($date,0,4)."-".substr($date,4,2)."-".substr($date,6,2);
                }else{
                    $this->setWarning("日付の書式が不正です( 生年月日 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }elseif($data_arr[$i]["BIRTHDAY"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/",$data_arr[$i]["BIRTHDAY"])){
                $this->setWarning("日付の書式が不正です( 生年月日 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if($this->IsDate($data_arr[$i]["BIRTHDAY"]) == false){
               $this->setWarning("日付が不正です( 生年月日 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
               return false;
            }
            if(is_numeric($data_arr[$i]["CREATE_DATE"])){
                $date = $data_arr[$i]["CREATE_DATE"];
                if(strlen($date) == 8){
                    $data_arr[$i]["CREATE_DATE"] = substr($date,0,4)."-".substr($date,4,2)."-".substr($date,6,2);
                }else{
                    if($this->target == "01"){
                        $this->setWarning("日付の書式が不正です( 作成日付 ) \\n氏名：".$data_arr[$i]["NAME"]);
                        return false;
                    }else{
                        $this->setWarning("日付の書式が不正です( 作成日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                        return false;
                    }
                }
            }elseif($data_arr[$i]["CREATE_DATE"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/",$data_arr[$i]["CREATE_DATE"])){
                if($this->target == "01"){
                    $this->setWarning("日付の書式が不正です( 作成日付 ) \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }else{
                    $this->setWarning("日付の書式が不正です( 作成日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            if($this->IsDate($data_arr[$i]["CREATE_DATE"]) == false){
                if($this->target == "01"){
                    $this->setWarning("日付の書式が不正です( 作成日付 ) \\n氏名：".$data_arr[$i]["NAME"]);
                    return false;
                }else{
                    $this->setWarning("日付の書式が不正です( 作成日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }
            if(is_numeric($data_arr[$i]["PS_UPDATED"])){
                $date = $data_arr[$i]["PS_UPDATED"];
                if(strlen($date) == 8){
                    $data_arr[$i]["PS_UPDATED"] = substr($date,0,4)."-".substr($date,4,2)."-".substr($date,6,2);
                }else{
                    $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }elseif($data_arr[$i]["PS_UPDATED"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/",$data_arr[$i]["PS_UPDATED"])){
                $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if($this->IsDate($data_arr[$i]["PS_UPDATED"]) == false){
               $this->setWarning("日付が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
               return false;
            }
            if(is_numeric($data_arr[$i]["FS_UPDATED"])){
                $date = $data_arr[$i]["FS_UPDATED"];
                if(strlen($date) == 8){
                    $data_arr[$i]["FS_UPDATED"] = substr($date,0,4)."-".substr($date,4,2)."-".substr($date,6,2);
                }else{
                    $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }elseif($data_arr[$i]["FS_UPDATED"]!="" && !preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/",$data_arr[$i]["FS_UPDATED"])){
                $this->setWarning("日付の書式が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
                return false;
            }
            if($this->IsDate($data_arr[$i]["FS_UPDATED"]) == false){
               $this->setWarning("日付が不正です( 登録日付 ) \\n受験番号：".$data_arr[$i]["EXAMNO"]);
               return false;
            }

            //データ型チェック
            if(($data_arr[$i]["A_SCORE"] != "" && !is_numeric($data_arr[$i]["A_SCORE"])) ||
               ($data_arr[$i]["B_SCORE"] != "" && !is_numeric($data_arr[$i]["B_SCORE"])) ){
                $this->warning = "Ａ得点かＢ得点のいずれかが数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                return false;
            }

            //1：事前相談データのみ
            if($this->target == "01"){
                if($data_arr[$i]["PS_ITEM1"] != "" && !is_numeric($data_arr[$i]["PS_ITEM1"])){
                    $this->warning = "塾の実力テスト得点が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                    return false;
                }elseif($data_arr[$i]["FS_ITEM1"] != "" && !is_numeric($data_arr[$i]["FS_ITEM1"])){
                    $this->warning = "出身学校の実力テスト得点が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                    return false;
                }
                for($pitem=2; $pitem<6; $pitem++){
                    $item = explode(".",$data_arr[$i]["PS_ITEM".$pitem]);
                    if($data_arr[$i]["PS_ITEM".$pitem] != "" & !(preg_match("/./",$data_arr[$i]["PS_ITEM".$pitem]) & is_numeric($data_arr[$i]["PS_ITEM".$pitem]))){
                        $this->warning = "塾の平均か偏差値が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                        return false;
                    }
                    if($item[0] > 999 || $item[0] < -999){    //2006.01.27 alp m-yama
                        $this->setWarning("MSG915","( 塾の平均か偏差値は1,000以下です。)  \\n氏名：".$data_arr[$i]["NAME"]."　値：".$item[0]);
                          return false;
                       }
                }
                for($fitem=2; $fitem<6; $fitem++){
                    $item = explode(".",$data_arr[$i]["FS_ITEM".$fitem]);
                    if($data_arr[$i]["FS_ITEM".$fitem] != "" & !(preg_match("/./",$data_arr[$i]["FS_ITEM".$fitem]) & is_numeric($data_arr[$i]["FS_ITEM".$fitem]))){
                        $this->warning = "出身学校の平均か偏差値が数値ではありません。\\n氏名：".$data_arr[$i]["NAME"];
                        return false;
                    }
                    if($item[0] > 999 || $item[0] < -999){    //2006.01.27 alp m-yama
                        $this->setWarning("MSG915","( 出身学校の平均か偏差値は1,000以下です。)  \\n氏名：".$data_arr[$i]["NAME"]."　値：".$item[0]);
                        return false;
                    }
                }
            }

            //5:志願者(内申)データのみ---
            if($this->target == "05"){
                for($rpt=1; $rpt<13; $rpt++){
                    if($data_arr[$i]["CONFIDENTIAL_RPT".$rpt] != "" && !is_numeric($data_arr[$i]["CONFIDENTIAL_RPT".$rpt])){
                        $this->warning = "教科".$rpt."が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                        return false;
                    }
                }
                $total_report = explode(".",$data_arr[$i]["TOTAL_REPORT"]);    //2006.02.03 alp m-yama
                if($data_arr[$i]["TOTAL_REPORT"] != "" & !(preg_match("/./",$data_arr[$i]["TOTAL_REPORT"]) & is_numeric($data_arr[$i]["TOTAL_REPORT"]))){    //2006.02.03 alp m-yama
                    $this->warning = "総評が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
                //2006.02.03 alp m-yama ↓
                if(strlen($total_report[0]) > 2){
                    $this->setWarning("MSG915","(総評)  受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
                //2006.02.03 alp m-yama ↑
                $average5 = explode(".",$data_arr[$i]["AVERAGE5"]);
                if($data_arr[$i]["AVERAGE5"] != "" & !(preg_match("/./",$data_arr[$i]["AVERAGE5"]) & is_numeric($data_arr[$i]["AVERAGE5"]))){
                    $this->warning = "５教科平均が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
                if(strlen($average5[0]) > 2){
                    $this->setWarning("MSG915","(５教科平均)  受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
                $avarage_all = explode(".",$data_arr[$i]["AVARAGE_ALL"]);
                if($data_arr[$i]["AVARAGE_ALL"] != "" & !(preg_match("/./",$data_arr[$i]["AVARAGE_ALL"]) & is_numeric($data_arr[$i]["AVARAGE_ALL"]))){
                    $this->warning = "全教科平均が数値ではありません。\\n受験番号：".$data_arr[$i]["EXAMNO"];
                    return false;
                }
                if(strlen($avarage_all[0]) > 2){
                    $this->setWarning("MSG915","( 全教科平均 )  受験番号：".$data_arr[$i]["EXAMNO"]);
                    return false;
                }
            }//--------------------------
        }

        Query::dbCheckIn($db);
        return true;
    }

    //日付の妥当性チェック
    function IsDate($datestr)
    {
        if ($datestr =="") return true;
        $tmp = explode(substr($datestr,4,1),$datestr);
        return checkdate($tmp[1],$tmp[2],$tmp[0]);
    }

    //テンプレートの書き出し
    /* ---------------------------------------------------- */
    /* 2005/10/19 minei                                     */
    /* ＣＳＶに 対象データ、試験区分、出願区分 などを付加   */
    /* ---------------------------------------------------- */
    function OutputTmpFile()
    {
        switch($this->target){
            case "01"://事前相談データ
                $contents  ="対象データ*,試験区分*,作成日付*,氏名*,氏名かな,性別コード,";
                $contents .="塾登録日付,塾コード,塾実力テスト得点,塾実力テスト平均,塾得点－平均,塾五ツ木偏差値,塾各種模擬偏差値,塾専併区分１,塾学科１,塾判定１,塾専併区分２,塾学科２,塾判定２,塾専併区分３,塾学科３,塾判定３,塾専併区分４,塾学科４,塾判定４,";
                $contents .="学校登録日付,学校コード,学校実力テスト得点,学校実力テスト平均,学校得点－平均,学校五ツ木偏差値,学校各種模擬偏差値,学校専併区分１,学校学科１,学校判定１,学校専併区分２,学校学科２,学校判定２,学校専併区分３,学校学科３,学校判定３,学校専併区分４,学校学科４,学校判定４\n";
                $contents .="1,1,20050101,山田太郎,やまだたろう,1,20050101,1,100,100,100,100,100,S,H,A,S,H,A,S,H,A,S,H,A,";
                $contents .="20050101,1,100,100,100,100,100,S,I,A,S,I,A,S,I,A,S,I,A";
                $filename = sprintf("%s.csv","事前相談データ");
                break;
            case "02"://志願者(速報用)データ
                $contents  ="対象データ*,受験番号*,受付日付*,専併区分*,志望区分*,性別コード*,国公私立区分コード*\n";
                $contents .="2,1001,20050101,H,6,1,2";
                $filename = sprintf("%s.csv","志願者速報用データ");
                break;
            case "03"://志願者(基礎データ
                $contents  ="対象データ*,試験区分*,出願区分*,受験番号*,受付日付,専併区分,志望区分,特別理由区分*,氏名*,氏名かな,性別コード,生年月日,現住所コード,電話番号,出身学校所在地コード,国公私立区分コード,出身学校コード,卒業年,出身学校組,出身学校出席番号,塾コード,通知承諾フラグ,保護者氏名,保護者かな,保護者電話番号,フォームNo.\n";
                $contents .="3,1,0,1001,20050101,S,1,0,山田太郎,やまだたろう,1,19900101,99,04-8765-4321,99,1,9999999,2005,999,999,9999999,1,山田花子,やまだはなこ,01-234-5678,999999";

                $filename = sprintf("%s.csv","志願者基礎データ");
                break;
            case "04"://志願者(住所)データ
                //2005/12/30 by AMEKU
                $contents  ="対象データ*,試験区分*,受験番号*,郵便番号,住所,保護者郵便番号,保護者住所\n";
                $contents .="4,1,1001,123-4567,東京都○○市○○町,123-4567,東京都○○市○○町";
                $filename = sprintf("%s.csv","志願者住所データ");
                break;
            case "05"://志願者(内申)データ
                $contents  ="対象データ*,受験番号*,教科１,教科２,教科３,教科４,教科５,教科６,教科７,教科８,教科９,教科１０,教科１１,教科１２,総評,５教科平均,全教科平均\n";
                $contents .="5,1001,11,12,13,14,15,16,17,18,19,20,21,22,99.9,99.9,99.9";    //2006.02.03 alp m-yama
                $filename = sprintf("%s.csv","志願者内申データ");
                break;
            case "06"://志願者(附属推薦または中高一貫)
                $contents  ="対象データ*,試験区分*,出願区分*,受験番号*,受付日付*,専併区分*,志望区分*,特別理由区分*,氏名*,氏名かな*,性別コード*,生年月日,現住所コード*,電話番号,出身学校所在地コード*,国公私立区分コード*,出身学校コード*,卒業年,出身学校組,出身学校出席番号,塾コード,通知承諾フラグ,保護者氏名,保護者かな,保護者電話番号,フォームNo.\n";  #2005/11/08 arakaki
                $contents .="6,1,1,1001,20050101,S,6,0,山田太郎,やまだたろう,1,19900101,99,04-8765-4321,99,1,9999999,2005,999,999,9999999,1,山田花子,やまだはなこ,01-234-5678,999999";    #2005/11/08 arakaki
                $filename = sprintf("%s.csv","中高一貫データ");
                break;
            case "07"://クラブ推薦者データ
                $contents  ="対象データ*,区分*,受験番号*,クラブコード\n";    //2006.01.17 alp m-yama
                $contents .="7,1,1001,9999";
                $filename = sprintf("%s.csv","クラブ推薦データ");    //2006.01.15 alp m-yama
                break;
            case "08"://得点データ
                $contents  ="対象データ*,試験区分*,受験番号*,試験科目コード*,Ａ得点,Ｂ得点\n";
                $contents .="8,1,1001,1,100,100";
                $filename = sprintf("%s.csv","志願者得点データ(".$this->testsubclassnm.")");
                break;

            case "09"://スカラ対象データ
                $contents  ="対象データ*,受験番号*,対象スカラコード*\n";
                $contents .="9,1001,99";
                $filename = sprintf("%s.csv","スカラ対象データ");
                break;
            case "10"://出身塾データ 2005/10/19 minei
                $contents  ="対象データ*,受験番号*,塾コード*\n";
                $contents .="10,1001,9999999";
                $filename = sprintf("%s.csv","出身塾データ");
                break;
        }
        common::downloadContents($contents, $filename);
        return true;
    }
}
?>
