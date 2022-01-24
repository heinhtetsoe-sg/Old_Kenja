<?php

require_once('for_php7.php');


class knje150Model extends Model {
    var $field          = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje150Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"                =>  VARS::post("YEAR"),             //年度
                            "GAKKI"                =>  VARS::post("GAKKI"),            //学期コード
                            "GRADE"                =>  VARS::post("GRADE"),            //学年
                            "OUT_DIV"              =>  VARS::post("OUT_DIV"),          //対象ラジオボタン 
                            "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        if ($this->field["OUT_DIV"] == "1") {
            return $this->printStudyrec();
        } else {
            return $this->printAttendrec();
        }
    }

    function printStudyrec()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knje150Query::getStudyrecCsv($this);
        $result = $db->query($query);
        $dataTemp = array();
        $tempCnt = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            //学籍
            if ($dataTemp[$row["SCHREGNO"]] == "") {
                $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["NAME_SHOW"];
            }
            //記録データ
            $dataTemp[$row["SCHREGNO"]][] = $row["ANNUAL"];
            $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
            $dataTemp[$row["SCHREGNO"]][] = $row["VALUATION"];
            $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];

            $tempCnt[$row["SCHREGNO"]]++;
        }
        $result->free();
        Query::dbCheckIn($db);
        //ヘッダ
        $header = array("SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名"
                        );
        $headCnt = 0;
        foreach ($tempCnt as $key => $val) {
            $headCnt = $headCnt < $val ? $val : $headCnt;
        }
        for ($i = 0; $i < $headCnt; $i++) {
            $header["ANNUAL".$i]       = "年次";
            $header["SUBCLASSNAME".$i] = "科目名";
            $header["VALUATION".$i]    = "評定";
            $header["GET_CREDIT".$i]   = "修得単位数";
        }
        $contents = implode($header, ",") ."\n";
        //データ
        $data = false;
        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";
            $data = true;
        }
        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", "学習の記録");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function printAttendrec()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knje150Query::getAttendrecCsv($this);
        $result = $db->query($query);
        $dataTemp = array();
        $tempCnt = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            //学籍
            if ($dataTemp[$row["SCHREGNO"]] == "") {
                $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["NAME_SHOW"];
            }
            //記録データ
            $dataTemp[$row["SCHREGNO"]][] = $row["ANNUAL"];
            $dataTemp[$row["SCHREGNO"]][] = $row["LESSON"];
            $dataTemp[$row["SCHREGNO"]][] = $row["SUSPEND_MOURNING"];
            $dataTemp[$row["SCHREGNO"]][] = $row["ABROAD"];
            $dataTemp[$row["SCHREGNO"]][] = $row["REQUIREPRESENT"];
            $dataTemp[$row["SCHREGNO"]][] = $row["PRESENT"];
            $dataTemp[$row["SCHREGNO"]][] = $row["ABSENT"];
            $dataTemp[$row["SCHREGNO"]][] = $row["LATE"];
            $dataTemp[$row["SCHREGNO"]][] = $row["EARLY"];

            $tempCnt[$row["SCHREGNO"]]++;
        }
        $result->free();
        Query::dbCheckIn($db);
        //ヘッダ
        $header = array("SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名"
                        );
        $headCnt = 0;
        foreach ($tempCnt as $key => $val) {
            $headCnt = $headCnt < $val ? $val : $headCnt;
        }
        for ($i = 0; $i < $headCnt; $i++) {
            $header["ANNUAL".$i]            = "年次";
            $header["LESSON".$i]            = "授業日数";
            $header["SUSPEND_MOURNING".$i]  = "出席停止・忌引等の日数";
            $header["ABROAD".$i]            = "留学中の授業日数";
            $header["REQUIREPRESENT".$i]    = "出席しなければならない日数";
            $header["PRESENT".$i]           = "出席日数";
            $header["ABSENT".$i]            = "欠席日数";
            $header["LATE".$i]              = "遅刻";
            $header["EARLY".$i]             = "早退";
        }
        $contents = implode($header, ",") ."\n";
        //データ
        $data = false;
        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";
            $data = true;
        }
        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", "出欠の記録");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
