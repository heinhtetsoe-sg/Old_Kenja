<?php

require_once('for_php7.php');

class knjd157dModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $selectleft;

    /**
     * メンバ変数の初期化
     */
    public function knjd157dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"           =>  VARS::post("YEAR"),          //年度
                             "SEMESTER"       =>  VARS::post("SEMESTER"),      //学期コード
                             "GRADE"          =>  VARS::post("GRADE"),         //学年
                             "CLASS_SELECTED" =>  VARS::post("CLASS_SELECTED"),//選択クラス
                             "TESTKINDCD"     =>  VARS::post("TESTKINDCD"),    //テスト種別
                             "HID_SCHKIND"    =>  VARS::post("HID_SCHKIND"),   //校種(画面更新前の学年選択時の校種)
                             "SELECT"         =>  VARS::post("SELECT"),        //選択したクラス
                             "DBNAME"         =>  VARS::post("DBNAME")         //DB変更に備えて
        );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $title = common::DateConv1($this->field["YEAR"]."/04/01", 2)."　";

        $query = knjd157dQuery::getTestName($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $title .= $row["TESTITEMNAME"]."　";    //テスト種別
            switch ($row["SCORE_DIV"]) {
                case "01":
                    $div = "(素点)";
                    break;
                case "08":
                    $div = "(評価)";
                    break;
                case "09":
                    $div = "(評定)";
                    break;
            }
        }
        $result->free();

        $title .= "　成績不振一覧表".$div;

        //ヘッダー
        $header = array("学年", "組", "番", "氏名");

        $subclasscd    = array();
        $studentDate   = array();
        $subclassScore = array();

        $contents  = $title."\n";

        //選択したクラス、学籍番号を分割
        $select = explode(",", $this->field["SELECT"]);
        array_pop($select);    //末尾の空白の要素を削除

        //選択クラス内で警告点以下の科目を取得
        $query = knjd157dQuery::getKeikokuSubclass($this, $select);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header[]     .= $row["SUBCLASSABBV"];  //科目名
            $subclasscd[] .= $row["SUBCLASSCD"];    //科目コード
        }
        $result->free();

        $header[] .= "未受験講座数";
        $header[] .= "欠点講座数";
        $header[] .= "欠点単位合計";

        $contents .= implode($header, ",")."\n";


        //生徒情報(欠点講座数等含む)を取得
        $query = knjd157dQuery::getStudentDate($this, $select);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $studentDate[ $row["SCHREGNO"] ] = array(  'GRADE'         => $row["GRADE"]         //学年
                                                     , 'HR_CLASS'      => $row["HR_CLASS_NAME1"]//クラス名
                                                     , 'ATTENDNO'      => $row["ATTENDNO"]      //番号
                                                     , 'NAME'          => $row["NAME"]          //氏名
                                                     , 'MIJUKEN_COUNT' => $row["MIJUKEN_COUNT"] //未受験講座数
                                                     , 'KETTEN_COUNT'  => $row["KETTEN_COUNT"]  //欠点講座数
                                                     , 'CREDITS_TOTAL' => $row["CREDITS_TOTAL"] //欠点合計単位
            );
        }
        $result->free();

        //生徒の科目ごとの点数を取得
        $query = knjd157dQuery::getSubclassScore($this, $select);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclassScore[ $row["SCHREGNO"] ][ $row["SUBCLASSCD"] ] = $row["SCORE"];
        }
        $result->free();

        foreach ($studentDate as $key => $schreg) {
            $sep = ",";
            $contents .= $schreg["GRADE"];
            $contents .= $sep.$schreg["HR_CLASS"];
            $contents .= $sep.$schreg["ATTENDNO"];
            $contents .= $sep.$schreg["NAME"];
            foreach ($subclasscd as $sub) {
                //科目コードと一致する点数をセット
                $contents .= $sep.$subclassScore[$key][$sub];
            }
            $contents .= $sep.$schreg["MIJUKEN_COUNT"];
            $contents .= $sep.$schreg["KETTEN_COUNT"];
            $contents .= $sep.$schreg["CREDITS_TOTAL"];
            $contents .= "\n";
        }

        $filename = sprintf("%s.csv", $title);
        common::downloadContents($contents, $filename, "SJIS-win");

        return true;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
