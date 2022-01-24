<?php

require_once('for_php7.php');

//メニュー画面
class knjz212Model extends model
{
    var $cmd;               //cmd
    var $semester;          //学期
    var $grade;             //学年
    var $update_data;       //更新データ

    function knjz212Model()
    {
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");
        $this->semester    = VARS::request("SEMESTER");
        $this->grade       = VARS::request("GRADE");
        $this->update_data = VARS::request("update_data");
        
        $this->getPropertiesAll(); //プロパティファイル
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
    }

    //コピー処理
    function getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (!$this->validateInput($db)) {
            return;
        }
        $db->query(knjz212Query::getCopyQuery($this));

        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput($db)
    {
        if (0 < $db->getOne(knjz212Query::getDataCnt())) {
            $this->setWarning("MSG203","今年度のデータが存在します。");
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $data = array();
        $data = preg_split("/,/", $this->update_data);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knjz212Query::getDeleteQuery($this));
        foreach ($data as $key => $val) {
            $upData = array();
            $upData = preg_split("/-/", $val);
            if ($this->Properties["useCurriculumcd"] == '1') {
                if ($upData[4] != "") {
                    $db->query(knjz212Query::getUpdateUseCurriculumQuery($this, $upData[0], $upData[1], $upData[2], $upData[3], $upData[4]));
                }
            } else {
                if ($upData[1] != "") {
                    $db->query(knjz212Query::getUpdateQuery($this, $upData[0], $upData[1]));
                }
            }
        }
        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

}
?>
