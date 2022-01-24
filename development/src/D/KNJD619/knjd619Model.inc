<?php

require_once('for_php7.php');

class knjd619Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $selectSemeName;
    var $selectTestName;

    /**
     * メンバ変数の初期化
     */
    function knjd619Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "SUBCLASS_MAX"     =>  VARS::post("SUBCLASS_MAX"),     //科目数選択
                             "FORM_SELECT"      =>  VARS::post("FORM_SELECT"),      //フォーム選択
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
        $this->selectSemeName = VARS::post("selectSemeName");
        $this->selectTestName = VARS::post("selectTestName");
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
                           "SEMESTER"       => $this->selectSemeName.",",
                           "TITLE"          => "仮評定一覧表（".$this->selectTestName."）,,",
                           "DATE"           => "作成日：".str_replace("-","/",CTRL_DATE)
                           );

        $header  = array("SCHREGNO"         => "学籍番号",
                         "GRADE"            => "学年",
                         "HR_CLASS"         => "組",
                         "ATTENDNO"         => "出席番号",
                         "NAME_SHOW"        => "生徒氏名",
                         "SEX"              => "性別"
                        );

        $header2 = array("AVERAGE"          => "評定平均");

        $subHeader = array("SCHREGNO"       => "",
                           "GRADE"          => "",
                           "HR_CLASS"       => "",
                           "ATTENDNO"       => "",
                           "NAME_SHOW"      => "",
                           "SEX"            => ""
                           );

        //ヘッダ配列を出力変数にセット
        $topHeaderContents  = implode($topHeader, $comma) .$lf;
        $headerContents     = implode($header, $comma) .$comma;
        $subHeaderContents  = implode($subHeader, $comma) .$comma;

        //科目コード・科目名の取得
        $this->arrSubclass = array();
        $datacheckflg = "";
        $query  = knjd619Query::getSubClass($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->arrSubclass[] = $row["VALUE"];
            $headerContents     .= $row["SUBCLASSNAME"] .$comma;
            $subHeaderContents  .= $row["VALUE"] .$comma;
            $datacheckflg = ($row["VALUE"]) ? true : false;
        }
        $result->free();
        $headerContents .= implode($header2, $comma) .$lf;
        $subHeaderContents .= $lf;

        //データ有無チェック
        if (!$datacheckflg) {
            $this->setWarning("MSG303");
            return false;
        }

        //メイン（成績）
        $dataContents = "";
        $data = false;  //CSV出力フラグ
        $query = knjd619Query::getCsvQuery($this);
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
        $contents .= $topHeaderContents;
        $contents .= $headerContents;
        $contents .= $subHeaderContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", "仮評定一覧表");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>