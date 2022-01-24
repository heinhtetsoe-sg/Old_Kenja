<?php

require_once('for_php7.php');

class knjs343Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ

    public function knjs343Model()
    {
    }

    public function init()
    {
        $this->rowCnt = 10; // 行数
        $this->sqlInDiCd = "'4', '5', '6'"; // 2:出停 4:病欠 5:事故欠 6:無届事故欠
        $this->koteiDiCd = "F1"; // F1:病・事故欠

        $this->field = array("HR_CLASS_TYPE"    => VARS::post("HR_CLASS_TYPE"),     //クラス方式
                             "GAKUNEN_KONGOU"   => VARS::post("GAKUNEN_KONGOU"),    //学年混合チェックボックス
                             "GRADE_HR_CLASS"   => VARS::post("GRADE_HR_CLASS"),    //対象クラス
                             "TARGET_MONTH"     => VARS::post("TARGET_MONTH"),      //対象月
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");

        for ($i=1; $i <= $this->rowCnt; $i++) {
            $this->field["CHECK-".$i]       = VARS::post("CHECK-".$i);      // 削除
            $this->field["DI_CD-".$i]       = VARS::post("DI_CD-".$i);      // 種別
            $this->field["TOTAL_DAY-".$i]   = VARS::post("TOTAL_DAY-".$i);  // 日数
            $this->field["NAME-".$i]        = VARS::post("NAME-".$i);       // 氏名(学籍)
            $this->field["REMARK-".$i]      = VARS::post("REMARK-".$i);     // 事由
            $this->field["GNAME-".$i]       = VARS::post("GNAME-".$i);      // 保護者氏名
            $this->field["TREATMENT-".$i]   = VARS::post("TREATMENT-".$i);  // 担任のとった処置
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //データチェック
    public function dataCheck()
    {
        for ($i=1; $i <= $this->rowCnt; $i++) {
            //空文字チェック
            if (strlen($this->field["NAME-".$i]) != 0) {
                if (strlen($this->field["DI_CD-".$i]) == 0) {
                    $this->setWarning("MSG301", "(種別) {$i}行目");
                    return false;
                }
                if (strlen($this->field["TOTAL_DAY-".$i]) == 0) {
                    $this->setWarning("MSG301", "(日数) {$i}行目");
                    return false;
                }
            }
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->dataCheck()) {
            return;
        }
        knjs343Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
