<?php

class knje372gModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;

    public function knje372gModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function getExecModel()
    {
        $db = Query::dbCheckOut();

        //学科毎の残り推薦枠数を取得
        $this->depLimitCnt = array();
        $depNameMap = array();
        $query = knje372gQuery::getDepLimitCnt();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->depLimitCnt[$row["DEPARTMENTCD"]] = $row["LIMIT_COUNT"];
            $depNameMap[$row["DEPARTMENTCD"]]  = $row["DEPARTMENT_NAME"]."(".$row["DEPARTMENT_DIV"].")";
        }

        $senganToHeiganMap = array();
        $query = knje372gQuery::getDepCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $senganToHeiganMap[$row["DEPARTMENT_S"]] = $row["DEPARTMENT_H"];
        }

        //振り分けデータ作成
        $this->allocatedSchMap = array();
        $query = knje372gQuery::getList($this);
        $result = $db->query($query);
        $errFlg = false;
        $errMsg = "";
        $allocatedRankCntArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //既に割り振り先学科が存在する生徒はスキップ
            if (array_key_exists($row["SCHREGNO"], $this->allocatedSchMap)) {
                continue;
            }

            //割振り可能かをチェックする学科をセット(専願の場合は不可の場合、併願の学科もチェックする)
            $targetDepCd = array();
            $targetDepCd[] = $row["DEPARTMENT_CD"];
            $heiganDepCd = $senganToHeiganMap[$row["DEPARTMENT_CD"]];
            if ($heiganDepCd) {
                $targetDepCd[] = $heiganDepCd;
            }

            foreach ($targetDepCd as $departmentCd) {
                if ($this->judgeAllocationOk($departmentCd, $row)) {
                    //生徒毎の割り振り先学科を保持
                    $this->allocatedSchMap[$row["SCHREGNO"]] = $departmentCd;
                    $this->depLimitCnt[$departmentCd]--;

                    //現在の換算値順位での割振り済み生徒数
                    $allocatedRankCntArray[$row["CONVERT_RANK"]]++;
                    break;
                } elseif ($allocatedRankCntArray[$row["CONVERT_RANK"]] >= 1) {
                    //同順の生徒が割振済みで、対象生徒は割振り不可な場合はエラーとしその時点で終了
                    $errFlg = true;
                    $errHopeOrder = intval($row["HOPE_ORDER"]);
                    $errMsg = "学籍番号:{$row["SCHREGNO"]}  換算値順位:{$row["CONVERT_RANK"]}位  第{$errHopeOrder}志望 {$departmentCd}:{$depNameMap[$departmentCd]} ";
                    break 2;
                } else {
                    //通常の割り振り不可の場合、専願学科なら併願学科が割振り可能かをチェックする
                    continue;
                }
            }
        }

        Query::dbCheckIn($db);

        if ($errFlg) {
            $this->setMessage("MSG203", "同順位内で学科振り分けに失敗した生徒が存在します。\\n\\n( {$errMsg} )");
        } elseif ($this->update()) {
            $this->setMessage("MSG201");
        }
    }

    //割振り不可判定関数
    public function judgeAllocationOk($departmentCd, $row)
    {
        //枠数の設定が無い学科はスキップ
        if (!array_key_exists($departmentCd, $this->depLimitCnt)) {
            return false;
        }
        //対象学科の残り枠数が0
        if ($this->depLimitCnt[$departmentCd] <= 0) {
            return false;
        }

        return true;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knje372gQuery::getUpdateQuery($this);
        return $result;
    }
}
