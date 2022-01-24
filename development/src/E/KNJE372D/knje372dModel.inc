<?php

require_once('for_php7.php');


/**
 * 定数定義
*/
class knje372dModel extends Model
{
    public $cmd;
    public $control;

    /**
     * メンバ変数の初期化
     */
    public function knje372dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field["SEMESTER"]        = VARS::post("SEMESTER");       //学期
        $this->field["GRADE"]           = VARS::post("GRADE");          //学年
        $this->field["TESTITEM"]        = VARS::post("TESTITEM");       //考査

        $this->field["GRADE_MAX"]       = VARS::post("GRADE_MAX");      //最大学年

        $this->field["PROFICIENCYDIV"]  = VARS::post("PROFICIENCYDIV"); //区分
        $this->field["PROFICIENCYCD"]   = VARS::post("PROFICIENCYCD");  //テスト

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //最終学期取得
        if ($this->maxSemester == "") {
            $db = Query::dbCheckOut();
            $query = knje372dQuery::getSemester(CTRL_YEAR, "");
            $result = $db->query($query);
            
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->maxSemester = $row["VALUE"];
            }
            Query::dbCheckIn($db);
        }
    }


    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $proficiencyMstList = array();
        // 名称マスタから「校内学力試験」の情報を取得
        $query = knje372dQuery::getNameMst('E072', '');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["NAME3"];

            //実力区分・コード
            $proficiencyMstList[$key]["PROFICIENCYDIV"] = $row["NAME1"];
            $proficiencyMstList[$key]["PROFICIENCYCD"]  = $row["NAME2"];
            //対象学年
            $proficiencyMstList[$key]["GRADE"] = $row["NAME3"];
            //対象科目
            $proficiencyMstList[$key]["SUBCLASS_CD1"] = $row["NAMESPARE1"];
            $proficiencyMstList[$key]["SUBCLASS_CD2"] = $row["NAMESPARE2"];

            //出欠換算値 作成フラグ
            $proficiencyMstList[$key]["ATTEND_ADJUST_FLG"] = $row["ABBV1"];
            //推薦基準区分 作成フラグ
            $proficiencyMstList[$key]["BASE_DIV_FLG"] = $row["ABBV2"];
        }
        $result->free();

        //学年一覧の取得
        $gradeList = array();
        $query = knje372dQuery::getGradeList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gradeList[] = $row["GRADE"];
        }

        // 校内学力試験データ作成
        knje372dQuery::executeProficiency($db, $this, $proficiencyMstList);

        // 総合成績データ更新
        knje372dQuery::executeRecordRankScore($db, $this, $gradeList);

        // 換算値更新
        knje372dQuery::executeConvertScore($db, $this);

        // 出欠情報更新
        if ($proficiencyMstList[$this->field["GRADE"]]["ATTEND_ADJUST_FLG"] == "1") {
            // 出欠情報更新
            knje372dQuery::executeAttendAdjust($db, $this, $gradeList);
        }

        // 換算値順位更新
        knje372dQuery::executeConvertScoreRank($db, $this);

        if ($proficiencyMstList[$key]["BASE_DIV_FLG"] == "1") {
            // 推薦基準区分更新 (RECOMMENDATION_BASE_DIV)
            knje372dQuery::executeRecommendationBaseDiv($db, $this, $gradeList);

            // 学科区分更新 (DEPARTMENT_BASE_DIV)
            knje372dQuery::executeDepartmentBaseDiv($db, $this, $gradeList);
        }

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG202");
    }
}
