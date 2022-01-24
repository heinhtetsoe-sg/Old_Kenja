<?php

require_once('for_php7.php');

class knjd154Model extends Model
{
    public $semester;
    public $hrClass;
    public $selectdata;
    public $selectdata2;
    public $cmd;
    public $control;        //コントロールマスタ
    public $tutisyoTokubetuKatudo;

    public function knjd154Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
    }

    public function init()
    {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),               //学期
                             "TYOUHYOU_PATTERN"       =>  VARS::post("TYOUHYOU_PATTERN"),       //帳票パターンラジオボタン
                             "AVG_PRINT"              =>  VARS::post("AVG_PRINT"),              //平均値出力選択チェックボックス
                             "AVG_DIV"                =>  VARS::post("AVG_DIV"),                //平均値ラジオボタン
                             "NO_PRINT_STUDENT_NAME"  =>  VARS::post("NO_PRINT_STUDENT_NAME"),  //生徒名出力なし
                             "NO_PRINT_STUDENT_NAME2" =>  VARS::post("NO_PRINT_STUDENT_NAME2"), //生徒名出力なし2
                             "OKURIJOU_JUSYO"         =>  VARS::post("OKURIJOU_JUSYO"),         //送り状住所ラジオボタン
                             "TYUI_TYOUKA"            =>  VARS::post("TYUI_TYOUKA"),            //注意・超過ラジオボタン
                             "FORM_SELECT"            =>  VARS::post("FORM_SELECT"),            //A4角形封筒
                             "JUSYO_PRINT"            =>  VARS::post("JUSYO_PRINT"),            //送り状住所なしチェックボックス
                             "DATE_DIV"               =>  VARS::post("DATE_DIV"),               //出欠集計範囲（累計・学期）ラジオボタン
                             "DATE"                   =>  VARS::post("DATE"),                   //出欠集計範囲（終了日付）
                             "ATTENDREC"              =>  VARS::post("ATTENDREC"),              //出欠の記録の欄なし チェックボックス
                             "TOTALSTUDY"             =>  VARS::post("TOTALSTUDY"),             //総合的な学習の時間の欄なし チェックボックス
                             "CORRE"                  =>  VARS::post("CORRE"),                  //通信欄なし チェックボックス
                             "SHR_KESSEKI_NASI"       =>  VARS::post("SHR_KESSEKI_NASI"),       //ＳＨＲの欠席時数なし チェックボックス
                             "HANREI_SYUTURYOKU_NASI" =>  VARS::post("HANREI_SYUTURYOKU_NASI"), //凡例出力なし チェックボックス
                             "HR_CLASS_TYPE"          =>  VARS::post("HR_CLASS_TYPE")           //実クラス選択
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
