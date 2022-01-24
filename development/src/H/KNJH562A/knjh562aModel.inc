<?php

require_once('for_php7.php');

class knjh562aModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $proficiencyDiv; //試験区分
    public $proficiencyCd;  //試験コード
    public $rownum;
    public $lowers;
    public $rownum_min = 3;
    public $rownum_max = 11;
    public $max_score = 400;
    public $min_score = 0;

    public function init()
    {
        $this->year   = CTRL_YEAR;
        $this->cmd    = VARS::request("cmd");
        $this->lowers = VARS::post("LOWER_LIMIT");

        if (isset($this->semester) == false) {
            $this->semester = CTRL_SEMESTER;
        }

        //試験区分を初期化
        switch ($this->cmd) {
            case "cancel":
                $this->proficiencyDiv = "";
                break;
            default:
                $this->proficiencyDiv = VARS::post("PROFICIENCYDIV");
                break;
        }

        //試験コードを初期化
        switch ($this->cmd) {
            case "change_prfcencydiv":
            case "cancel":
                $this->proficiencyCd = "";
                break;
            default:
                $this->proficiencyCd  = VARS::post("PROFICIENCYCD");
                break;
        }

        //行数指定値を初期化
        switch ($this->cmd) {
            case "change_row":
                $this->rownum = VARS::post("ROWNUM");
                break;

            case "update":
            case "change_prfcencydiv":
            case "change_prfcencycd":
                if (is_array($this->lowers) && 0 < count($this->lowers)) {
                    //入力行数を基準とする
                    $this->rownum = count($this->lowers);
                }
                break;

            case "cancel":
                //登録済みデータ件数に依存
                break;

            default:
                $this->rownum = 7;
                break;
        }

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //学校種別の指定有無を取得
    public function isIndicateSchoolKind()
    {
        return ($this->Properties["useSchool_KindField"] == "1" && $this->selectSchoolKind != "");
    }

    //入力チェック
    public function validateInput(&$scoreList)
    {
        if (is_array($scoreList) == false) {
            $this->setWarning("MSG901", "( 引数エラー )");
            return false;
        }

        //未入力チェック
        if (isset($this->proficiencyDiv) == false) {
            $this->setWarning("MSG301", "( データ種別 )");
            return false;
        }
        if (isset($this->proficiencyCd) == false) {
            $this->setWarning("MSG301", "( テスト名称 )");
            return false;
        }
        if (is_array($this->lowers) == false) {
            $this->setWarning("MSG301", "( 下限値 )");
            return false;
        }

        //入力行数確認
        $rowCount = count($this->lowers);
        if ($rowCount < $this->rownum_min) {
            $this->setWarning("MSG301", "( 下限値 )");
            return false;
        }

        //各行の下限値入力チェック
        for ($idx =0; $idx < $rowCount; $idx++) {
            //行番号
            $rowNo = $idx + 1;

            //未入力チェック
            if (strlen($this->lowers[$idx]) < 1) {
                $this->setWarning("MSG301", "( 下限値 {$rowNo}行目 )");
                return false;
            }
            //半角数字チェック
            if (preg_match('/^[0-9]+$/', $this->lowers[$idx]) == false) {
                $this->setWarning("MSG907", "( 下限値 {$rowNo}行目 )");
                return false;
            }
            $targetScore = (int)$this->lowers[$idx];

            //入力範囲チェック
            if ($targetScore < $this->min_score || $this->max_score < $targetScore) {
                $this->setWarning("MSG914", "( 下限値 {$rowNo}行目 )\n最小：{$this->min_score} ～ 最大：{$this->max_score}");
                return false;
            }

            //降順・重複チェック
            for ($upprIdx = $idx -1; 0 <= $upprIdx; $upprIdx--) {
                $upperScore = (int)$this->lowers[$upprIdx];
                if ($targetScore > $upperScore) {
                    $this->setWarning("MSG914", "( 下限値 {$rowNo}行目 )");
                    return false;
                }
                if ($targetScore == $upperScore) {
                    $this->setWarning("MSG302", "( 下限値 {$rowNo}行目 )");
                    return false;
                }
            }

            //チェックを通過した点数設定をリストへプールしておく
            $scoreList[$rowNo] = array(
                "tick_low"  => $targetScore,
                "tick_high" => $idx < 1 ? $this->max_score : ( (int)$this->lowers[$idx -1] -1 )
            );
        }

        return true;
    }

    //度数分布刻みテーブル更新
    public function updateLimitScore()
    {
        //点数範囲設定値
        $scoreList = array();

        //入力チェック
        if ($this->validateInput($scoreList) == false) {
            unset($scoreList);
            return false;
        }

        //登録・更新実行
        knjh562aQuery::modifyProcifiensyTickWidth($this, $scoreList);

        //更新完了通知
        $this->setMessage("MSG201");
    }
}
