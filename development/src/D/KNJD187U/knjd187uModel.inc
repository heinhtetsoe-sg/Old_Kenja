<?php

require_once('for_php7.php');

class knjd187uModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata;
    public $print;

    public function knjd187uModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");
    
        $this->field = array("OUTPUT_DATE"      =>  VARS::post("OUTPUT_DATE"),       //出力日付
                             "DATE"             =>  VARS::post("DATE"),              //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "TESTCD"           =>  VARS::post("TESTCD"),            //考査種別
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );
        $this->selectdata   = VARS::post("selectdata");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        
        if ($this->cmd == 'knjd187u') {
            $this->selectdata = null;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        //各textareaのタイトル
        $this->remarkTitle = array(
            "1"  =>  "追記事項　上段"
         ,  "2"  =>  "追記事項　下段"
        );

        //各textareaの最大文字数
        $this->remarkTextLimit = array(
               "1"  =>  array("moji" => 23, "gyou" =>  4)
            ,  "2"  =>  array("moji" => 23, "gyou" =>  2)
        );

        //テキストエリアの値取得
        foreach ($this->remarkTextLimit as $key => $textLimit) {
            $this->field["REMARK".$key] = VARS::post("REMARK".$key);
        }
    }
    
    //入力項目チェック
    public function validateInput()
    {
        //文字数チェック
        foreach ($this->remarkTextLimit as $seq => $textLimit) {
            $moji = $textLimit["moji"];
            $gyou = $textLimit["gyou"];

            if ($this->validate_row_cnt($this->field["REMARK{$seq}"], ($moji * 2)) > $gyou) {
                $this->setWarning("MSG915", "({$this->remarkTitle[$seq]}は{$moji}文字X{$gyou}行までです。)");
                return false;
            }
        }
        return true;
    }
    
    public function &getCheckModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->print = "on";
    }
}
