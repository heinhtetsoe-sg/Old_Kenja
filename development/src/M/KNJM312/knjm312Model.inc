<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm312Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm312Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),
                             "DEADLINE_DATE"    =>  VARS::post("DEADLINE_DATE")
                     );
        $this->cmd = VARS::request("cmd");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        $this->update();
    }

    function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $repStandardArray = array();
        $query = knjm312Query::getRepStandard($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $repStandardArray[] = $row;
        }

        foreach ($repStandardArray as $key => $val) {
            knjm312Query::getUpdateQuery($db, $this, $val);
        }

        $this->setMessage("MSG201");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
