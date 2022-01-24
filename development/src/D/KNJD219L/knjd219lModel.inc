<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd219lModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;
    var $isMyp;

    /**
     * メンバ変数の初期化
     */
    function knjd219lModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER" ),    //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "OUTPUT_DIV"   =>  VARS::post("OUTPUT_DIV"),   //出力種別
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    
    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjd219lQuery::getMypDp($this);
        $result = $db->query($query);
        $this->isMyp = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAME1"] == 'M') {
                $this->isMyp = "1";
            }
        }

        $query = knjd219lQuery::getSelectData($this);
        $result = $db->query($query);
        $checkSchregno = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($checkSchregno != $row["SCHREGNO"].'-'.$row["IBYEAR"]) {
                //Delete
                knjd219lQuery::DeleteQuery($db, $row, $this);
            }
            //Insert
            knjd219lQuery::updateQuery($db, $row, $this);
            $checkSchregno = $row["SCHREGNO"].'-'.$row["IBYEAR"];
        }
        $this->setMessage("MSG201");
        
        $db->commit();
        Query::dbCheckIn($db);
        
        return;
    }
    
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
