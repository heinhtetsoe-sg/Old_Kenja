<?php

require_once('for_php7.php');

class knjl313cModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isCollege;

    /**
     * メンバ変数の初期化
     */
    function knjl313cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "DBNAME"	    =>  VARS::post("DBNAME")            //DB変更に備えて
                            );


        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl313cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

	/**
	 * 
	 */
    function getDownloadModel()
    {
		//ヘッダの作成//
        if (!$this->isGojou) { //和歌山
            $header = array("APPDIV_NAME"       => "入試制度",
                            "TESTDIV_NAME"      => "入試区分",
                            "EXAMNO"            => "受験番号",
                            "NAME_KANA"         => "かな",
                            "NAME"              => "氏名",
                            "SHDIV"             => "専併",
                            "SEX"               => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "ZIPCD"             => "郵便番号",
                            "PREF_NAME"         => "県別",
                            "ADDRESS"           => "現住所",
                            "EDBOARDNAME"       => "教育委員会",
                            "TELNO"             => "電話番号",
                            "DISTRICT_NAME"     => "所在地",
                            "FS_DIST_NAME"      => "区分",
                            "FINSCHOOL_NAME"    => "出身学校",
                            "GKANA"             => "保護者かな",
                            "GNAME"             => "保護者氏名",
                            "RELATIONSHIP"      => "続柄",
                            "GZIPCD"            => "保護者郵便番号",
                            "GPREF_NAME"        => "保護者県別",
                            "GADDRESS"          => "保護者現住所",
                            "GTELNO"            => "保護者電話番号",
                            "REMARK"            => "備考"
                            );
        } else if ($this->isCollege) { //カレッジ
            $header["APPDIV_NAME"]      = "入試制度";
            $header["TESTDIV_NAME"]     = "入試区分";
            $header["EXAMNO"]           = "受験番号";
            $header["NAME_KANA"]        = "かな";
            $header["NAME"]             = "氏名";
            $header["SHDIV"]            = "専願";
            $header["HOPE_COURSE"]      = "志望コース";
            if ($this->field["APPLICANTDIV"] == "1" && ($this->field["TESTDIV"] == "1" || $this->field["TESTDIV"] == "9")) {
                $header["SUB_ORDER"]      = "受験型";
            }
            $header["SEX"]              = "性別";
            $header["BIRTHDAY"]         = "生年月日";
            $header["ZIPCD"]            = "郵便番号";
            $header["PREF_NAME"]        = "県別";
            $header["ADDRESS"]          = "現住所";
            $header["PRISCHOOL_NAME"]   = "塾名";
            $header["FINSCHOOL_NAME"]   = "出身学校";
            $header["GKANA"]            = "保護者かな";
            $header["GNAME"]            = "保護者氏名";
            $header["RELATIONSHIP"]     = "続柄";
            $header["GZIPCD"]           = "保護者郵便番号";
            $header["GPREF_NAME"]       = "保護者県別";
            $header["GADDRESS"]         = "保護者現住所";
            $header["GTELNO"]           = "保護者電話番号";
            $header["REMARK"]           = "備考";
        } else { //五條
            $header = array("APPDIV_NAME"       => "入試制度",
                            "TESTDIV_NAME"      => "入試区分",
                            "EXAMNO"            => "受験番号",
                            "SLIDE_FLG"         => "",
                            "SPORTS_FLG1"       => "Ｓ１",
                            "SPORTS_FLG2"       => "Ｓ２",
                            "NAME_KANA"         => "かな",
                            "NAME"              => "氏名",
                            "SHDIV"             => "専願",
                            "SEX"               => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "ZIPCD"             => "郵便番号",
                            "PREF_NAME"         => "県別",
                            "ADDRESS"           => "現住所",
                            "PRISCHOOL_NAME"    => "塾名",
                            "FINSCHOOL_NAME"    => "出身学校",
                            "GKANA"             => "保護者かな",
                            "GNAME"             => "保護者氏名",
                            "RELATIONSHIP"      => "続柄",
                            "GZIPCD"            => "保護者郵便番号",
                            "GPREF_NAME"        => "保護者県別",
                            "GADDRESS"          => "保護者現住所",
                            "GTELNO"            => "保護者電話番号",
                            "DORMITORY_FLG"     => "入寮",
                            "REMARK"            => "備考"
                            );
        }

        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $certif_kindcd = ($this->field["APPLICANTDIV"] == "1") ? '105' : '106';
        $query3 = knjl313cQuery::getSchoolName($this->ObjYear, $certif_kindcd);
        $schoolname = $db->getOne($query3);

        //年度取得
        $nxtyr_date = (substr(CTRL_DATE,0,4)+1).(substr(CTRL_DATE,4));
	    $year = common::DateConv1(str_replace("-","/",$nxtyr_date),10);

        //日付・時間取得
	    $date = common::DateConv1(str_replace("-","/",CTRL_DATE),0).date(" H時i分現在");

        $contents  = $year."　志願者チェックリスト\n";
        $contents .= $date.",,,,,,,,,,,,,,,,,,".$schoolname."\n";
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $query = knjl313cQuery::selectCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if (strlen($row["OTHER_TESTDIV_NAME"])) {
                $row["RECOM_EXAMNO"] = $row["OTHER_TESTDIV_NAME"].' '.$row["RECOM_EXAMNO"].' ';
            } else {
                $row["RECOM_EXAMNO"] = "";
            }

            if (!$this->isGojou) { //和歌山
                $row["SHIFT_DESIRE_FLG"] = ($row["SHIFT_DESIRE_FLG"] == "") ? "" : '移行希望 ';
                $temp = array("APPDIV_NAME"     => $row["APPDIV_NAME"],
                              "TESTDIV_NAME"    => $row["TESTDIV_NAME"],
                              "EXAMNO"          => $row["EXAMNO"],
                              "NAME_KANA"       => $row["NAME_KANA"],
                              "NAME"            => $row["NAME"],
                              "SHDIV"           => $row["SHDIV"],
                              "SEX"             => $row["SEX"],
                              "BIRTHDAY"        => $row["BIRTHDAY"],
                              "ZIPCD"           => $row["ZIPCD"],
                              "PREF_NAME"       => $row["PREF_NAME"],
                              "ADDRESS"         => $row["ADDRESS1"].$row["ADDRESS2"],
                              "EDBOARDNAME"     => $row["EDBOARDNAME"],
                              "TELNO"           => $row["TELNO"],
                              "DISTRICT_NAME"   => $row["DISTRICT_NAME"],
                              "FS_DIST_NAME"    => $row["FS_DIST_NAME"],
                              "FINSCHOOL_NAME"  => $row["FINSCHOOL_NAME"],
                              "GKANA"           => $row["GKANA"],
                              "GNAME"           => $row["GNAME"],
                              "RELATIONSHIP"    => $row["RELATIONSHIP"],
                              "GZIPCD"          => $row["GZIPCD"],
                              "GPREF_NAME"      => $row["GPREF_NAME"],
                              "GADDRESS"        => $row["GADDRESS1"].$row["GADDRESS2"],
                              "GTELNO"          => $row["GTELNO"],
                              "REMARK"          => $row["RECOM_EXAMNO"].$row["SHIFT_DESIRE_FLG"].$row["REMARK1"].$row["REMARK2"]
                             );
            } else if ($this->isCollege) { //カレッジ
                if ($row["SHIFT_DESIRE_FLG"] == "")  $row["SHIFT_DESIRE_FLG"] = "";
                if ($row["SHIFT_DESIRE_FLG"] == "1") $row["SHIFT_DESIRE_FLG"] = "五併 ";
                if ($row["SHIFT_DESIRE_FLG"] == "2") $row["SHIFT_DESIRE_FLG"] = "和併 ";
                if ($row["SHIFT_DESIRE_FLG"] == "3") $row["SHIFT_DESIRE_FLG"] = "五併/和併 ";
                $temp["APPDIV_NAME"]    = $row["APPDIV_NAME"];
                $temp["TESTDIV_NAME"]   = $row["TESTDIV_NAME"];
                $temp["EXAMNO"]         = $row["EXAMNO"];
                $temp["NAME_KANA"]      = $row["NAME_KANA"];
                $temp["NAME"]           = $row["NAME"];
                $temp["SHDIV"]          = $row["SHDIV"];
                $temp["HOPE_COURSE"]    = $row["HOPE_COURSE"];
                if ($this->field["APPLICANTDIV"] == "1" && ($this->field["TESTDIV"] == "1" || $this->field["TESTDIV"] == "9")) {
                    $temp["SUB_ORDER"]      = $row["SUB_ORDER"];
                }
                $temp["SEX"]            = $row["SEX"];
                $temp["BIRTHDAY"]       = $row["BIRTHDAY"];
                $temp["ZIPCD"]          = $row["ZIPCD"];
                $temp["PREF_NAME"]      = $row["PREF_NAME"];
                $temp["ADDRESS"]        = $row["ADDRESS1"].$row["ADDRESS2"];
                $temp["PRISCHOOL_NAME"] = $row["PRISCHOOL_NAME"];
                $temp["FINSCHOOL_NAME"] = $row["FINSCHOOL_NAME"];
                $temp["GKANA"]          = $row["GKANA"];
                $temp["GNAME"]          = $row["GNAME"];
                $temp["RELATIONSHIP"]   = $row["RELATIONSHIP"];
                $temp["GZIPCD"]         = $row["GZIPCD"];
                $temp["GPREF_NAME"]     = $row["GPREF_NAME"];
                $temp["GADDRESS"]       = $row["GADDRESS1"].$row["GADDRESS2"];
                $temp["GTELNO"]         = $row["GTELNO"];
                $temp["REMARK"]         = $row["RECOM_EXAMNO"].$row["SHIFT_DESIRE_FLG"].$row["REMARK1"].$row["REMARK2"];
            } else { //五條
                $row["SHIFT_DESIRE_FLG"] = ($row["SHIFT_DESIRE_FLG"] == "") ? "" : 'カレッジ併願 ';
                $temp = array("APPDIV_NAME"     => $row["APPDIV_NAME"],
                              "TESTDIV_NAME"    => $row["TESTDIV_NAME"],
                              "EXAMNO"          => $row["EXAMNO"],
                              "SLIDE_FLG"       => "",
                              "SPORTS_FLG1"     => $row["SPORTS_FLG1"],
                              "SPORTS_FLG2"     => $row["SPORTS_FLG2"],
                              "NAME_KANA"       => $row["NAME_KANA"],
                              "NAME"            => $row["NAME"],
                              "SHDIV"           => $row["SHDIV"],
                              "SEX"             => $row["SEX"],
                              "BIRTHDAY"        => $row["BIRTHDAY"],
                              "ZIPCD"           => $row["ZIPCD"],
                              "PREF_NAME"       => $row["PREF_NAME"],
                              "ADDRESS"         => $row["ADDRESS1"].$row["ADDRESS2"],
                              "PRISCHOOL_NAME"  => $row["PRISCHOOL_NAME"],
                              "FINSCHOOL_NAME"  => $row["FINSCHOOL_NAME"],
                              "GKANA"           => $row["GKANA"],
                              "GNAME"           => $row["GNAME"],
                              "RELATIONSHIP"    => $row["RELATIONSHIP"],
                              "GZIPCD"          => $row["GZIPCD"],
                              "GPREF_NAME"      => $row["GPREF_NAME"],
                              "GADDRESS"        => $row["GADDRESS1"].$row["GADDRESS2"],
                              "GTELNO"          => $row["GTELNO"],
                              "DORMITORY_FLG"   => $row["DORMITORY_FLG"],
                              "REMARK"          => $row["RECOM_EXAMNO"].$row["SHIFT_DESIRE_FLG"].$row["REMARK1"].$row["REMARK2"]
                             );
            }

	        $contents .= implode($temp, ",") ."\n";
            $data = true;
        }

        $result->free();

        //集計人数
        $boy = knjl313cQuery::selectCsvQuery($this, "1");
        $girl = knjl313cQuery::selectCsvQuery($this, "2");

        $contents .= "\n";
        $contents .= "男 ".get_count($db->getcol($boy))."名　女 ".get_count($db->getcol($girl))."名　計 ".get_count($db->getcol($query))."名\n";

        Query::dbCheckIn($db);

        if ($data){
	        $couse="志願者チェックリスト";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
