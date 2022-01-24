<?php

require_once('for_php7.php');

class knjb170Model extends Model {
    var $cmd;
    var $obj_radio;     //出力種別
    var $obj_year;
    var $obj_semester;
    var $obj_schoolcd;
    var $obj_majorcd;
    var $obj_saiken;
    var $controls = array();
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjb170Model() {}

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

        common::GetControlMaster_Fnc($this->controls);

        $this->cmd          = VARS::request("cmd");
        $this->obj_radio    = VARS::post("RADIO");
        $this->obj_year     = VARS::post("YEAR");
        $this->obj_semester = "1";
        $this->obj_schoolcd = VARS::post("SCHOOLCD");
        $this->obj_majorcd  = VARS::post("MAJORCD");
        $this->obj_saiken   = VARS::post("SAIKEN");
        $this->getPropertiesAll(); //プロパティファイル
    }

    //ＣＳＶ出力
    function OutputTmpFile() {
        $db  = Query::dbCheckOut();

        //単位情報
        if ($this->obj_radio == 1)
            $contents = "学年,組,出席番号,氏名,学校コード,課程コード,授業料年度,生徒番号,債権種別,科目数,合計単位数,併修フラグ,授業料電算に渡すデータ(科目数),授業料電算に渡すデータ(単位数)\n";
        //生徒情報
        if ($this->obj_radio == 2)
            $contents = "学校コード,課程コード,授業料年度,学籍番号,生徒氏名(カナ・半角),生徒氏名(漢字),保護者氏名(カナ・半角),保護者氏名(漢字),授業料電算に渡すデータ(生徒情報)\n";

        $data = false;
        if ($this->obj_radio == 1) $result = $db->query(knjb170Query::getOutputQueryCsv1($this));
        if ($this->obj_radio == 2) $result = $db->query(knjb170Query::getOutputQueryCsv2($this));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->obj_radio == 1) {
                $row["CNT_SUB"] = sprintf("%02d", $row["CNT_SUB"]);
                $row["CNT_CRE"] = sprintf("%02d", $row["CNT_CRE"]);
                $row["DENSAN_SUB"] = $row["SCHOOLCD"] .$row["MAJORCD"] .sprintf("%04d", $row["NENDO"]) .sprintf("%08d", $row["SCHREGNO"]) .$row["SAIKEN"] .$row["CNT_SUB"] .$row["FLG"];
                $row["DENSAN_CRE"] = $row["SCHOOLCD"] .$row["MAJORCD"] .sprintf("%04d", $row["NENDO"]) .sprintf("%08d", $row["SCHREGNO"]) .$row["SAIKEN"] .$row["CNT_CRE"] .$row["FLG"];
            }
            if ($this->obj_radio == 2) {
                $row["NAME_KANA"]  = i18n_ja_jp_hantozen(str_replace('　', ' ', $row["NAME_KANA"]), "khV");
                $row["GUARD_KANA"] = i18n_ja_jp_hantozen(str_replace('　', ' ', $row["GUARD_KANA"]), "khV");
                //授業料電算に渡すデータを生成
                $wareki = $this->obj_year - 1988;
                //氏名・半角氏名
                $name_kana     = preg_replace("/[A-Za-z ]/","",$row["NAME_KANA"]); //半角スペースを除いた文字数をカウント
                $name_kana_len = (mb_strlen($name_kana) * 2 + 20); //半角なのに3バイト見たいなので計算が必要
                if ($name_kana_len >= 60) {
                    $name_kana_res = sprintf("%.60s", $row["NAME_KANA"]);
                } else {
                    $name_kana_res = sprintf("%' -{$name_kana_len}s", $row["NAME_KANA"]);
                }

                $name     = preg_replace("/[A-Za-z ]/","",$row["NAME"]); //半角スペースを除いた文字数をカウント
                $name_len = (mb_strlen($name) + 40); //全角は2文字分の幅を取るけど、3バイトらしい
                if ($name_len >= 60) {
                    $name_res = sprintf("%.60s", $row["NAME"]);
                } else {
                    $name_res = sprintf("%' -{$name_len}s", $row["NAME"]);
                }

                //保護者氏名・半角氏名
                $guard_kana     = preg_replace("/[A-Za-z ]/","",$row["GUARD_KANA"]); //半角スペースを除いた文字数をカウント
                $guard_kana_len = (mb_strlen($guard_kana) * 2 + 20);
                if ($guard_kana_len >= 60) {
                    $guard_kana_res = sprintf("%.60s", $row["GUARD_KANA"]);
                } else {
                    $guard_kana_res = sprintf("%' -{$guard_kana_len}s", $row["GUARD_KANA"]);
                }

                $guard_name     = preg_replace("/[A-Za-z ]/","",$row["GUARD_NAME"]); //半角スペースを除いた文字数をカウント
                $guard_name_len = (mb_strlen($guard_name) + 40);
                if ($guard_name_len >= 60) {
                    $guard_name_res = sprintf("%.60s", $row["GUARD_NAME"]);
                } else {
                    $guard_name_res = sprintf("%' -{$guard_name_len}s", $row["GUARD_NAME"]);
                }

                $row["DENSAN"] = sprintf("%' -5s", $row["SCHOOLCD"]) .
                                 sprintf("%' -3s", $row["MAJORCD"]) .
                                 sprintf("%'02s",$wareki) .
                                 sprintf("%' -8s", $row["SCHREGNO"]) .
                                 $name_kana_res .
                                 $name_res .
                                 $guard_kana_res .
                                 $guard_name_res .
                                 "                                   ";
            }
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->obj_radio == 1) $filename = sprintf("%s.csv","(単位情報)授業料" ."_".$this->obj_year ."_".$this->obj_schoolcd ."_".$this->obj_majorcd ."_".$this->obj_saiken);
            if ($this->obj_radio == 2) $filename = sprintf("%s.csv","(生徒情報)授業料" ."_".$this->obj_year ."_".$this->obj_schoolcd ."_".$this->obj_majorcd);
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //チェック情報ＣＳＶ出力
    function getDownloadCheckModel() {
        $header = array("YEAR"          => "年度",
                        "SEMESTER"      => "学期",
                        "SCHREGNO"      => "学籍番号",
                        "GRADE"         => "学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "NAME_SHOW"     => "生徒氏名",
                        "SUBCLASSCD"    => "科目コード",
                        "SUBCLASSNAME"  => "科目名称",
                        "CREDITS"       => "単位数",
                        "AUTHORIZE_FLG" => "半期認定フラグ",
                        "COMP_UNCONDITION_FLG" => "無条件履修修得フラグ");

        //DB接続
        $db = Query::dbCheckOut();

        $data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        $query = knjb170Query::selectStudySubjectData($this, $this->obj_year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row); //データ内にカンマがあればドットに置換(置換する文字は、カンマでなければ何でも良い)
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data){
            $couse="チェック情報";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

    function stringFix($string, $length, $head_or_tail, $replace) {
        
    }
}
?>
