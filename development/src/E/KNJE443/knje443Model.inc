<?php

require_once('for_php7.php');

class knje443Model extends Model {
    var $field = array();
    var $cmd;
    var $z010Abbv1;
    var $schoolM;
    var $grd_grade;

    function knje443Model() {
        $this->field = array();
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->z010Abbv1);
            unset($this->schoolcd);
            unset($this->grd_grade);
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("FIXED_DATA"           =>  VARS::post("FIXED_DATA"),
                             "DOC_NUMBER"           =>  VARS::post("DOC_NUMBER"),
                             "EXECUTE_DATE"         =>  VARS::post("EXECUTE_DATE"),
                             "SCHOOL_GROUP_TYPE"    =>  VARS::post("SCHOOL_GROUP_TYPE"),
                             "IDOU_DATE"            =>  VARS::post("IDOU_DATE"),
                             "FIXED_DATE"           =>  VARS::post("FIXED_DATE")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $db = Query::dbCheckOut();

        //教育委員会判定
        $this->z010Abbv1 = $db->getOne(knje443Query::z010Abbv1());

        //学校マスタ取得
        $this->schoolM = $db->getRow(knje443Query::getSchoolMst($this), DB_FETCHMODE_ASSOC);

        //学校コード
        if ($this->z010Abbv1 == "1" || $this->z010Abbv1 == "2") {
            $this->schoolcd = $this->schoolM["KYOUIKU_IINKAI_SCHOOLCD"];
        } else {
            $this->schoolcd = "000000000000";
        }

        //卒業可能学年範囲
        $this->grd_grade = $db->getRow(knje443Query::getGradeRange(), DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knje443Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &fixedUpdate() {
        knje443Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・確定テーブル
        //学校側・報告テーブル
        knje443Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル）
        $title = "大学進学者数";
        $sakuseibi  = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $contents .= '年度：'.CTRL_YEAR.'年,,,'.$title.str_repeat(",", 6).'作成日：'.$sakuseibi."\n";

        //初期化
        $header = array();

        //ヘッダ（2行目）
        $header["COURSENAME"]           = "課程";
        $header["MAJORNAME"]            = "学科";
        $header["SCHOOL_GROUP_NAME"]    = "大学系列";
        $header["SCHOOL_NAME"]          = "大学名";
        $header["GOUKAKU_G"]            = "現役合格者数";
        $header["GOUKAKU_K"]            = "既卒合格者数";
        $header["GOUKAKU_T"]            = "合格者数計";
        $header["NAITEI_G"]             = "現役進学者数";
        $header["NAITEI_K"]             = "既卒進学者数";
        $header["NAITEI_T"]             = "進学者数計";

        //ヘッダ（2行目）セット
        $headerList = $header_array = array();
        foreach ($header as $key => $val) {
            $headerList[]   = $val;
            $header_array[] = $key;
        }
        $contents .= implode($headerList, ",") ."\n";

        //各行数取得
        $cntData = $db->getRow(knje443Query::getCsvQuery($this, "cnt"), DB_FETCHMODE_ASSOC);

        //SQL文発行
        $data = false;
        $dataTemp = array();
        $query = knje443Query::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $sep = "";
            foreach ($header_array as $hkey) {
                //データセット
                $contents .= $sep.$row[$hkey];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = CTRL_YEAR."年度_大学進学者数";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
