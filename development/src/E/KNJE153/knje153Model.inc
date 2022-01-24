<?php

require_once('for_php7.php');

class knje153Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje153Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
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

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //出力形態
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //ＣＳＶ出力
    function getDownloadModel() {

        return $this->printStudyrec();
    }

    //ＣＳＶ出力
    function printStudyrec() {
        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knje153Query::getAttendDataCsv($this);
        $result = $db->query($query);
        $dataTemp = array();
        $cnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $dataTemp[$cnt]["SCHREGNO"]     = $row["SCHREGNO"];
            $dataTemp[$cnt]["GRADE"]        = $row["GRADE"];
            $dataTemp[$cnt]["HR_CLASS"]     = $row["HR_CLASS"];
            $dataTemp[$cnt]["ATTENDNO"]     = $row["ATTENDNO"];
            $dataTemp[$cnt]["NAME_SHOW"]    = $row["NAME_SHOW"];
            if ($this->field["OUTPUT"] == "2") {
                $dataTemp[$cnt]["YEAR"]         = $row["YEAR"];
            }
            $dataTemp[$cnt]["LESSON"]       = $row["LESSON"];
            $dataTemp[$cnt]["OFFDAYS"]      = $row["OFFDAYS"];
            $dataTemp[$cnt]["ABSENT"]       = $row["ABSENT"];
            $dataTemp[$cnt]["SUSPEND"]      = $row["SUSPEND"];
            $dataTemp[$cnt]["MOURNING"]     = $row["MOURNING"];
            $dataTemp[$cnt]["ABROAD"]       = $row["ABROAD"];
            $dataTemp[$cnt]["SICK"]         = $row["SICK"];
            $dataTemp[$cnt]["LATE"]         = $row["LATE"];
            $dataTemp[$cnt]["EARLY"]        = $row["EARLY"];
            if ($this->field["OUTPUT"] == "2") {
                $dataTemp[$cnt]["SCHOOLCD"]     = ($row["SCHOOLCD"] == "1") ? "*" : "";
            }

            $cnt++;
        }
        $result->free();

        //ヘッダー
        $header["SCHREGNO"]     = "学籍番号";
        $header["GRADE"]        = "学年";
        $header["HR_CLASS"]     = "組";
        $header["ATTENDNO"]     = "出席番号";
        $header["NAME_SHOW"]    = "生徒氏名";
        if ($this->field["OUTPUT"] == "2") {
            $header["YEAR"]         = "年度";
        }
        $header["LESSON"]       = "授業日数";
        $header["OFFDAYS"]      = "休学日数";
        $header["ABSENT"]       = "公欠日数";
        $header["SUSPEND"]      = "出停日数";
        $header["MOURNING"]     = "忌引日数";
        $header["ABROAD"]       = "留学日数";
        $header["SICK"]         = "欠席日数";
        $header["LATE"]         = "遅刻日数";
        $header["EARLY"]        = "早退日数";
        if ($this->field["OUTPUT"] == "2") {
            $header["SCHOOLCD"]     = "前籍校";
        }

        //ヘッダーをセット
        $contents = implode($header, ",") ."\n";

        //データ
        $data = false;
        foreach ($dataTemp as $key => $val) {
            //データをセット
            $contents .= implode($val, ",") ;
            $contents .= "\n";
            $data = true;
        }

        //ファイル名用学年取得
        $query = knje153Query::getGrade($this, $this->field["GRADE"]);
        $grade = $db->getOne($query);

        //出力形態表示
        if ($this->field["OUTPUT"] == "1") {
            $output = "（まとめ）";
        } else {
            $output = "（年度ごと）";
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $nendo = CTRL_YEAR."年度";
            $filename = sprintf("%s.csv", $nendo.$grade."出欠データ".$output);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
