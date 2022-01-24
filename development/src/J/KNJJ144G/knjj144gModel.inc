<?php

require_once('for_php7.php');

class knjj144gModel extends Model {
    var $semester;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjj144gModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array(
                             "GRADE"            =>  VARS::post("GRADE"),             //年
                             "SEX"              =>  VARS::post("SEX"),               //性別
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->selectdata == "") {
            $this->setWarning("MSG310","（出力対象一覧）");
            return false;
        }
        return true;
    }

    /***********/
    /* CSV出力 */
    /***********/
    function getDownloadModel() {

        if (!$this->validateInput()) {
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        //ヘッダ
        $header = array();
        $header["NUMBER_OF_TIMES"]  = "回数";
        $header["HR_NAME"]          = "クラス";
        $header["NAME"]             = "氏名";
        $header["METERS"]           = "距離";
        $header["RANK"]             = "順位";
        $header["TIME"]             = "タイム";
        $header["EVENT_DATE"]       = "実施日";
        $header["PRINCIPAL_NAME"]   = "校長名";

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ出力
        $data = false;
        $query = knjj144gQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";

            //距離
            $row["METERS"] = rtrim($row["MAN_METERS"], "0");
            $row["METERS"] = rtrim($row["METERS"], ".");
            $row["METERS"] = $row["METERS"]."Km";
            if ($row["SEX"] == "2") {
                $row["METERS"] = rtrim($row["WOMEN_METERS"], "0");
                $row["METERS"] = rtrim($row["METERS"], ".");
                $row["METERS"] = $row["METERS"]."Km";
            }
            //タイム
            $row["TIME"] = "";
            if ($row["TIME_H"]) {
                $row["TIME"] .= $row["TIME_H"]."時間";
            }
            if ($row["TIME_M"] != "") {
                $row["TIME"] .= sprintf("%02d", $row["TIME_M"])."分";
            }
            if ($row["TIME_S"] != "") {
                $row["TIME"] .= sprintf("%02d", $row["TIME_S"])."秒";
            }
            //実施日
            $row["EVENT_DATE"] = str_replace("-", "/", $row["EVENT_DATE"]);
            //校長名(先頭スペース削除)
            $row["PRINCIPAL_NAME"] = preg_replace('/^[ 　]+/u', '', $row["PRINCIPAL_NAME"]);

            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = CTRL_YEAR."年度マラソン大会完走証";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
