<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjs570Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjs570Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"     =>  VARS::post("YEAR"),
                             "GRADE"    =>  VARS::post("GRADE")
                     );

        $this->cmd = VARS::request("cmd");
        $this->selectData = VARS::post("selectdata");
    }

    function getUpdateModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $delQuery = knjs570Query::getDelQuery($this);
        $db->query($delQuery);

        if ($this->selectData) {
            $updArray = preg_split("/,/", $this->selectData);
            foreach ($updArray as $val) {
                $query = knjs570Query::getInsQuery($this, $val, $updArray);
                $db->query($query);
            }
        }

        $db->commit();

        $this->setMessage("MSG201");

        //DB切断
        Query::dbCheckIn($db);
    }
    
    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $this_year_check = $db->getOne(knjs570Query::getStudyClassCount($this, $this->field["YEAR"]));
        $last_year_check = $db->getOne(knjs570Query::getStudyClassCount($this, $this->field["YEAR"] - 1));

        if($this_year_check != 0) {
            $this->setWarning("MSG203","既にデータが存在します。");
            return false;
        } else if($last_year_check == 0) {
            $this->setWarning("MSG303");
            return false;
        } else {
            $query = knjs570Query::getStudyClassData($this, $this->field["YEAR"] - 1);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //前年度のデータを今年度のデータとしてINSERT
                $query = knjs570Query::getInsQuery($this, $row["SUBCLASSCD"],$row);
                $db->query($query);
            }
        }
        $db->commit();

        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);
    }
    
}
?>
