<?php

require_once('for_php7.php');

class knjl444mModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjl444mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),                  //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),           //年組
                             "SEMESTER"            =>  VARS::post("SEMESTER"),              //学期コード
                             "DATE"               =>  VARS::post("DATE"),                   //調査日付
                             "OUTPUT"          =>  VARS::post("OUTPUT"),                    //フォーム選択
                             "PRINT_SCHREGNO"          =>  VARS::post("PRINT_SCHREGNO"),    //学籍番号を印字する
                             "DOCUMENTROOT"       =>  VARS::post("DOCUMENTROOT"),
                             "DBNAME"          =>  VARS::post("DBNAME"),                    //DB変更に備えて
                             "EXAM_SELECTED_KEY" =>  VARS::post("EXAM_SELECTED_KEY"),
                             "SCHOOL_KIND_DIV" => VARS::post("SCHOOL_KIND_DIV")
                     );


        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        // ヘッダ行
        $contents  = "受験番号,氏名,氏名かな,合否,手続金フラグ,入学金フラグ,辞退フラグ,繰り上げ合格"."\n";

        //DB接続
        $db = Query::dbCheckOut();

        $data = false;
        $keys = explode(",", $this->field["EXAM_SELECTED_KEY"]);

        $query = knjl444mQuery::getCSVdata($this->test_year, $keys);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= $row["RECEPTNO"].",";              // 受験番号
            $contents .= $row["NAME"].",";                  // 氏名
            $contents .= $row["NAME_KANA"].",";             // 氏名かな

            if ($row["JUDGEMENT"] == '2') {                  // 合否('2'の場合は、'1'に変換)
                $judgement = '1';
            } else {
                $judgement = $row["JUDGEMENT"];
            }
            $contents .= $judgement.",";

            $contents .= $row["DEPOSIT"].",";               // 手続金フラグ
            $contents .= $row["FEE"].",";                   // 入学金フラグ
            $contents .= $row["DECLINE"].",";               // 辞退フラグ
            $contents .= $row["JUDGEMENT"]=='2'? '2': '';   // 繰り上げ合格（'2'の場合のみ出力）
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);

        if ($data) {
            $couse="事務用志願者一覧";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
