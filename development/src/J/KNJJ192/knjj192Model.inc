<?php

require_once('for_php7.php');

class knjj192Model extends Model {
    var $field      = array();
    var $selectdata = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjj192Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    function clean() {}

    //ＣＳＶ出力処理
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //年組取得
        $query = knjj192Query::getFirstGh($this);
        $firstGhCheck = $db->getOne($query);

        //初期化
        $ghCheck = "";
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        //データ出力
        $query = knjj192Query::getDownloadCsv($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //クラス毎に改行
            if ($firstGhCheck != $row["GRADEHR"]) {
                $contents .= "\n";
            }
            if ($ghCheck != $row["GRADEHR"]) {
                //ヘッダー１
                $header = array();
                $header["HR_CLASS_NAME2"] = $row["HR_CLASS_NAME2"]."ホーム".",";
                $header["STAFFNAME"]      = "担任：".$row["STAFFNAME"].",";
                $header["STAFFTELNO"]     = "電話：".$row["STAFFTELNO"];
                //ヘッダー１出力
                $contents .= implode($header, ",")."\n";

                //ヘッダー２
                $header2 = array();
                $header2["ATTENDNO"]         = "No";
                $header2["GUARD_NAME"]       = "会員名";
                $header2["NAME"]             = "生徒名";
                $header2["FINSCHOOL_NAME"]   = "生徒出身校名";
                $header2["GUARD_ZIPCD"]      = "郵便番号";
                $header2["GUARD_ADDR"]       = "住所";
                $header2["GUARD_TELNO"]      = "電話番号";
                $header2["BRANCHNAME"]       = "支部名";
                $header2["BROSIS"]           = "本校在籍兄弟姉妹";
                //ヘッダー２出力
                $contents .= implode($header2, ",")."\n";
            }
            $dataTmp = array();
            $dataTmp["ATTENDNO"]       = $row["ATTENDNO"];
            $dataTmp["GUARD_NAME"]     = $row["GUARD_NAME"];
            $dataTmp["NAME"]           = $row["NAME"];
            $dataTmp["FINSCHOOL_NAME"] = $row["FINSCHOOL_NAME"];
            $dataTmp["GUARD_ZIPCD"]    = $row["GUARD_ZIPCD"];
            $dataTmp["GUARD_ADDR"]     = $row["GUARD_ADDR1"].$row["GUARD_ADDR2"];
            $dataTmp["GUARD_TELNO"]    = $row["GUARD_TELNO"];
            $dataTmp["BRANCHNAME"]     = $row["BRANCHNAME"];
            //本校在籍兄弟姉妹取得
            $sepFam = "";
            $query = knjj192Query::getFamilyCsv($row["FAMILY_NO"]);
            $resultFam = $db->query($query);
            while ($getFam = $resultFam->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (null != $getFam["SCHREGNO"] && $getFam["SCHREGNO"] == $row["SCHREGNO"]) {
                    continue;
                }
                if ($getFam["GRD_DIV"] == "") {
                    $row["BROSIS"] .= $sepFam.$getFam["HR_NAME"]."　".$getFam["NAME"];
                    $sepFam = "、";
                }
            }
            $dataTmp["BROSIS"]         = $row["BROSIS"];

            $ghCheck = $firstGhCheck = $row["GRADEHR"];

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header2 as $field => $label) {
                $contents .= $sep.$dataTmp[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        //ファイル名
        $title = CTRL_YEAR.'年度 連絡名簿';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>