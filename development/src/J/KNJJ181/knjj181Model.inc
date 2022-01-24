<?php

require_once('for_php7.php');


class knjj181Model extends Model {
    var $cmd;       //コマンド
    var $year;      //今年度
    var $year_add;  //次年度

    function knjj181Model() {
    }

    function init() {
        $this->year     = CTRL_YEAR;
        $this->year_add = CTRL_YEAR + 1;

        $this->checked  = VARS::request("CHECKED");     //チェック
        $this->cmd      = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($this->checked); $i++) {
            $Row = $db->getRow(knjj181Query::getFieldQuery($this->checked[$i], $this->year), DB_FETCHMODE_ASSOC);
            $query = knjj181Query::getInsertQuery($this->year, $this->year_add, $this->checked[$i], $Row, $this);

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
