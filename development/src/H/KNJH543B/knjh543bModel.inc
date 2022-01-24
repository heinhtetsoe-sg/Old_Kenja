<?php

require_once('for_php7.php');

class knjh543bModel extends Model {

    var $field = array();
    var $cmd;

    function knjh543bModel() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),   //実力区分
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),    //実力コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "DIV"              =>  sprintf('%02d', VARS::post("DIV")), //区分(03:コース 04:グループ)
                             "COURSE"           =>  VARS::post("COURSE"),           //課程学科コース
                             "GROUP_CD"         =>  VARS::post("GROUP_CD")          //コースグループ
                             );
        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput($db)
    {
        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput($db)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $calcRow = $db->getRow(knjh543bQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
            $calcDate = $calcRow["CALC_DATE"];//実行日付
            $calcTime = $calcRow["CALC_TIME"];//実行時間
            for ($i = 0; $i < get_count($this->combSubclass); $i++) {
                $combSubclass = $this->combSubclass[$i];//合併先科目コード
                $gvalCalc = VARS::post("GVAL_CALC" .$i);//計算方法
                //実行チェック
                $check = VARS::post("CHECK" .$i);
                if ($check != "1") {
                    continue;//処理しない
                }
                //実力科目合併自動計算履歴データ・追加
                $query = knjh543bQuery::getInsertRireki($combSubclass, $gvalCalc, $calcDate, $calcTime, $this);
                $db->query($query);
                ////////////////////////////////////////////////////////////
                //親の実行時、子を先に生成した後、親を生成する
                //合併元先科目があればそれを先に生成する・・・合併設定（親・子・孫）の対応
                $query = knjh543bQuery::getListMotoSaki($this, $combSubclass);
                $resultMotoSaki = $db->query($query);
                while ($rowMotoSaki = $resultMotoSaki->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $combSubclassMotoSaki   = $rowMotoSaki["COMBINED_SUBCLASSCD"];
                    $gvalCalcMotoSaki       = $rowMotoSaki["GVAL_CALC"];
                    //処理生徒の合併先科目の素点を自動計算
                    $query = knjh543bQuery::getScore($this, $combSubclassMotoSaki, $gvalCalcMotoSaki);
                    $resultScore = $db->query($query);
                    while ($rowScore = $resultScore->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $schregNo   = $rowScore["SCHREGNO"];
                        $score      = $rowScore["SCORE"];
                        $scoreDI    = $rowScore["SCORE_DI"];
                        //削除
                        $query = knjh543bQuery::getDeleteQuery($this, $schregNo, $combSubclassMotoSaki);
                        $db->query($query);
                        //追加
                        $query = knjh543bQuery::getInsertQuery($this, $schregNo, $combSubclassMotoSaki, $score, $scoreDI);
                        $db->query($query);
                    }
                    $resultScore->free();
                }
                $resultMotoSaki->free();
                ////////////////////////////////////////////////////////////
                //処理生徒の合併先科目の素点を自動計算
                $query = knjh543bQuery::getScore($this, $combSubclass, $gvalCalc);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $schregNo   = $row["SCHREGNO"];
                    $score      = $row["SCORE"];
                    $scoreDI    = $row["SCORE_DI"];
                    //削除
                    $query = knjh543bQuery::getDeleteQuery($this, $schregNo, $combSubclass);
                    $db->query($query);
                    //追加
                    $query = knjh543bQuery::getInsertQuery($this, $schregNo, $combSubclass, $score, $scoreDI);
                    $db->query($query);
                }
                $result->free();
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
