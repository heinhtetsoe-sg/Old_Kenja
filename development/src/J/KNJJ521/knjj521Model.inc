<?php
/**
 * 定数定義
*/
class knjj521Model extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $gen_ed = "900100";        //総合学習コード
    public $select;
    public $nyuryoku;
    public $type_div;

    /**
     * メンバ変数の初期化
     */
    public function knjj521Model()
    {
        $this->field = array();
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE")              //測定日付
                            );

        $this->cmd     = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //問診項目Max番号
        $this->maxInquiryNum = 6;
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //算出処理
    public function getCalcModel()
    {
        knjj521Query::execCalc($this);
        $this->setMessage("MSG201");
    }

    public function &getUpdateModel()
    {
        /*  更新処理 */

        //初期化
        $this->fields = array();
        for ($gyoNo = 0; $gyoNo < count($this->data["SCHREGNO"]); $gyoNo++) {
            //問診項目
            $inquiryCdArray = range(1, $this->maxInquiryNum);
            foreach ($inquiryCdArray as $inquiryCd) {
                $name = "INQUIRY".$inquiryCd;
                $term_name = $name."-".$gyoNo;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$name][$gyoNo] = $post_term;
                }
            }
            //種目
            foreach ($this->data["RECORD"] as $lenNo => $itemCd) {
                $name = "RECORD".$lenNo;
                $term_name = $name."-".$gyoNo;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$name][$gyoNo] = $post_term;
                }
            }
            //総合計
            $name = "TOTAL";
            $term_name = $name."-".$gyoNo;
            $post_term = VARS::post($term_name);
            if (isset($post_term)) {
                $this->fields[$name][$gyoNo] = $post_term;
            }
            //総合判定
            $name = "VALUE";
            $term_name = $name."-".$gyoNo;
            $post_term = VARS::post($term_name);
            if (isset($post_term)) {
                $this->fields[$name][$gyoNo] = $post_term;
            }
            //性別
            $name = "SEX";
            $term_name = $name."-".$gyoNo;
            $post_term = VARS::post($term_name);
            if (isset($post_term)) {
                $this->fields[$name][$gyoNo] = $post_term;
            }
            //誕生日
            $name = "BIRTHDAY";
            $term_name = $name."-".$gyoNo;
            $post_term = VARS::post($term_name);
            if (isset($post_term)) {
                $this->fields[$name][$gyoNo] = $post_term;
            }
        }
        //更新
        knjj521Query::update($this);
        $this->setMessage("MSG201");
        $this->cmd = ($this->select == "1") ? "form1" : "form1";
    }
}
