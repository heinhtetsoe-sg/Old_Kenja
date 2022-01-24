<?php

require_once('for_php7.php');


class knjp700Model extends Model {

    var $cmd;       //コマンド
    var $year;      //今年度
    var $year_add;  //次年度

    function knjp700Model()
    {
    }

    function init()
    {
        $this->year     = CTRL_YEAR;
        $this->year_add = CTRL_YEAR + 1;

        $this->checked  = VARS::request("CHECKED");     //チェック
        $this->cmd      = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($this->checked); $i++) {
            $Row = $db->getRow(knjp700Query::getFieldQuery($this->checked[$i], $this->year), DB_FETCHMODE_ASSOC);
            $field = $setField = "";
            $sep = "";
            foreach ($Row as $key => $val) {
                //自治体補助金マスタ、就学支援金マスタは、REDUCTION_SEQにデータはセットしない。自動でされる為
                if ($this->checked[$i] == "REDUCTION_MST" || $this->checked[$i] == "REDUCTION_COUNTRY_MST") {
                    if ($key == "REDUCTION_SEQ") {
                        continue;
                    }
                }
                $setField .= $sep.$key;

                if ($key == "YEAR") {
                    $key = "'$this->year_add'";
                } else if ($key == "REGISTERCD") {
                    $key = "'".STAFFCD."'";
                } else if ($key == "UPDATED") {
                    $key = "SYSDATE()";
                }
                $field .= $sep.$key;
                $sep = ",";
            }
            $query = knjp700Query::getInsertQuery($this->year, $this->year_add, $this->checked[$i], $field, $setField);

            if ($query) {
                $db->query($query);
            }
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

}
?>
