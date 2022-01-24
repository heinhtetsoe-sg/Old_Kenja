<?php
class knjd619eModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectGradeName;
    var $selectSemeName;
    var $selectTestName;

    /**
     * メンバ変数の初期化
     */
    function knjd619eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //考査種別
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->selectGradeName = VARS::post("selectGradeName");
        $this->selectSemeName  = VARS::post("selectSemeName");
        $this->selectTestName  = VARS::post("selectTestName");
    }

    //ＣＳＶ出力
    function getDownloadModel() {

        //変数定義
        $comma = ",";   //カンマ
        $lf = "\n";     //改行

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ配列
        $topHeader = array("YEAR"           => CTRL_YEAR."年度",
                           "GRADE"          => $this->selectGradeName,
                           "SEMESTER"       => $this->selectSemeName,
                           "TITLE"          => "基準点未到達生徒一覧（".$this->selectTestName."）,,",
                           "DATE"           => "作成日：".str_replace("-","/",CTRL_DATE)
                           );

        //出力項目
        $header  = array("GRADE"            => "年",
                         "HR_CLASS"         => "組",
                         "ATTENDNO"         => "番",
                         "SCHREGNO"         => "学籍番号",
                         "NAME"             => "生徒氏名",
                         "SUBCLASSCD"       => "科目コード",
                         "SUBCLASSNAME"     => "科目名称",
                         "SCORE"            => "評価"
                        );

        //ヘッダ配列を出力変数にセット
        $topHeaderContents  = implode($topHeader, $comma) .$lf;
        $headerContents     = implode($header, $comma) .$lf;

        //メイン
        $dataContents = "";
        $data = false;  //CSV出力フラグ
        $query = knjd619eQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace($comma, ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $dataContents .= implode($row, $comma) .$lf;
            $data = true;   //CSV出力フラグ
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        $contents  = "";
        //$contents .= $topHeaderContents;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = CTRL_YEAR."年度 ".$this->selectSemeName." 基準点未到達生徒一覧（".$this->selectTestName."）";
            $filename = sprintf("%s.csv", $filename);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
