<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd501kModel.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjd501kModel extends Model
{
    var $cmd;
    var $semes;             //学期
    var $grade;             //学年
    var $subcd;             //科目
    function knjd501kModel()
    {
        ;;
    }

    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->semes         = VARS::post("SEMES");
        $this->grade         = VARS::post("GRADE");
        $this->subcd         = VARS::post("SUBCD");
        //プロパティファイル
        $this->getPropertiesAll();
        //セキュリティーチェック
        $this->staffcd = STAFFCD ;
        $this->sec_competence = AUTHORITY;

        if ($this->cmd == "update") {
            $this->checked = array();
            $this->fieldno = array();
            $this->checked  = VARS::request("CHK_BOX");     //チェック
            for ($cnt = 0; $cnt < get_count($this->checked); $cnt++) {
                $this->fieldno[$cnt]   = preg_split("/-/", $this->checked[$cnt]);
                $this->fieldno[$cnt][3] = VARS::post("SEM".$this->semes."_REC".$this->fieldno[$cnt][0]);
            }
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //得点入力画面編集対象データの書込み処理
        for ($i = 0; $i < get_count($this->fieldno); $i++) {
            //画面データ更新
            $query = knjd501kQuery::getUpdateQuery($this, $this->fieldno[$i]);
            $db->query($query);
            //学年成績・評定更新
            $row = $db->getRow(knjd501kQuery::getRecordDat($this, $this->fieldno[$i]),DB_FETCHMODE_ASSOC);
            $query = knjd501kQuery::getGradeRecordUpQuery($db, $this, $this->fieldno[$i], $row);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>
