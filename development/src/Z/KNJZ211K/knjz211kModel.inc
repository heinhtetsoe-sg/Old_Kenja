<?php
class knjz211kModel extends Model {

    var $field       = array();
    var $cmd;
    var $Clean;
    var $sec_competence;            //権限保持
    var $TBL_COUNT;                 //テーブルの最大レコード数
    var $asses_cd;
    var $year;

    //Constructor
    function knjz211kModel()
    {
    }

    function init()
    {
        $this->year = CTRL_YEAR;

        //セキュリティーチェック値
        $this->sec_competence = AUTHORITY;

        if ($this->cmd == "reset" || $this->cmd == "update") {
            $this->asses_cd     = VARS::request("ASSES_CD");
        } else {
            $this->asses_cd     = VARS::get("ASSES_CD");
        }
        $this->asses_cd     = (isset($this->asses_cd)) ? $this->asses_cd : "";

        $this->field = array("YEAR"                 => $this->year,
                             "TYPE_ASSES_CD"        => VARS::post("ASSES_CD"),
                             "TYPE_GROUP_AVE_HIGH"  => VARS::post("AVE_HIGH"),
                             "TYPE_GROUP_AVE_LOW"   => VARS::post("AVE_LOW"),
                             "TYPE_ASSES_HIGH"      => VARS::post("ASSESSHIGH"),
                             "UPDATED"              => VARS::post("UPDATED") );

        //フィールドのレコード数取得
        $this->field["TBL_COUNT"] = VARS::post("TBL_COUNT");

        //テーブルのレコードの数だけ取得
        for ($i = 1; $i <= $this->field["TBL_COUNT"]; $i++) {
            $this->field["ASSESSLOW".($i)]  = VARS::post("ASSESSLOW".($i));
        }

        $this->cmd          = VARS::request("cmd");
        $this->Clean        = VARS::post("Cleaning");
    }

    function validateInput()
    {
        $chk[] = array();

        //field値チェック
        if ($this->field["TYPE_ASSES_CD"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        //UPDATEチェック
        if(!is_null($this->field["UPDATED"]) && $this->field["UPDATED"] != ""){

            //DBオープン
            $db     = Query::dbCheckOut();
            $result = $db->query(knjz211kQuery::selectQuery($this->year, $this->asses_cd));
            $up     = "";

            while($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($up == ""){
                    $up  =     $row["UPDATED"];
                }else{
                    $up .= ",".$row["UPDATED"];
                }
            }
            //DBクローズ
            Query::dbCheckIn($db);

            if ($this->field["UPDATED"] != $up) {
                $this->setWarning("MSG307");
                return false;
            }
        }

        //最大値チェック
        if (!isset($this->field["TBL_COUNT"])){
            $this->setWarning("MSG917");
            return false;
        }

        //テキストエリア入力チェック
        for($i = 1; $i <= $this->field["TBL_COUNT"]; $i++)
        {

            $ASSESSLOW  = "ASSESSLOW".($i);

            if(!isset($this->field[$ASSESSLOW])){
                $this->setWarning("MSG301");
                return false;
            }
            //空かどうか？
            if($this->field[$ASSESSLOW] == ""){
                $this->setWarning("MSG301");
                return false;
            }
            $chk[] = $this->field[$ASSESSLOW];
        }

        //値大小チェック
        for ($i = 1; $i <= $this->field["TBL_COUNT"]; $i++)
        {
            if ($i == 1) {
                if ($chk[$i] < 0) {
                    $this->setWarning("MSG901");
                    return false;
                }
            } else {
                if ($chk[$i] <= $chk[($i - 1)]) {
                    $this->setWarning("MSG901");
                    return false;
                }
            }
            if ($this->field["TYPE_ASSES_HIGH"] > 100) {
                $this->setWarning("MSG901");
                return false;
            }
            if ($i == $this->field["TBL_COUNT"]) {
                if ($chk[$i] > $this->field["TYPE_ASSES_HIGH"]) {
                        $this->setWarning("MSG901");
                    return false;
                }
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
    }

    function &update()
    {
        $result = false;
        //評定マスタデータ削除
        if (knjz211kQuery::getDeleteQuery($this->field)) {
        //評定マスタデータ追加
            if (knjz211kQuery::getInsertQuery($this->field)) {
                $result = true;
            }
        }
        return $result;
    }

    function &getCopyModel()
    {
        if (knjz211kQuery::getCopyQuery($this->year)) {
            $this->setMessage("MSG201");
        }

        return $result;
    }
}
?>
