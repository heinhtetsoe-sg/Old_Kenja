<?php

require_once('for_php7.php');

class knje434Model extends Model {
    var $field = array();
    var $cmd;
    var $z010Abbv1;
    var $schoolM;
    var $grd_grade;

    function knje434Model() {
        $this->field = array();
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->z010Abbv1);
            unset($this->schoolcd);
            unset($this->grd_grade);
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("FIXED_DATA"       =>  VARS::post("FIXED_DATA"),
                             "EXECUTE_DATE"     =>  VARS::post("EXECUTE_DATE"),
                             "IDOU_DATE"        =>  VARS::post("IDOU_DATE"),
                             "FIXED_DATA"       =>  VARS::post("FIXED_DATA"),
                             "FIXED_DATE"       =>  VARS::post("FIXED_DATE")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $db = Query::dbCheckOut();

        //教育委員会判定
        $this->z010Abbv1 = $db->getOne(knje434Query::z010Abbv1());

        //学校マスタ取得
        $this->schoolM = $db->getRow(knje434Query::getSchoolMst($this), DB_FETCHMODE_ASSOC);

        //学校コード
        if ($this->z010Abbv1 == "1" || $this->z010Abbv1 == "2") {
            $this->schoolcd = $this->schoolM["KYOUIKU_IINKAI_SCHOOLCD"];
        } else {
            $this->schoolcd = "000000000000";
        }

        //卒業可能学年範囲
        $this->grd_grade = $db->getRow(knje434Query::getGradeRange(), DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knje434Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &fixedUpdate() {
        knje434Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・確定テーブル
        //学校側・報告テーブル
        knje434Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル）
        $title = "就職状況集計表";
        $sakuseibi  = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $contents .= '年度：'.CTRL_YEAR.'年,,'.$title.str_repeat(",", 6).'作成日：'.$sakuseibi."\n";

        //初期化
        $header = array();

        //ヘッダ（2行目、3行目）
        $header["SEQ_NAME"]         = array("",         "就職地域");
        $header["PREF_NAME"]        = array("",         "就職都道府県");
        $header["COMPANY_NAME"]     = array("",         "事業所名");
        $header["GOUKAKU_M"]        = array("合格者",   "男");
        $header["GOUKAKU_F"]        = array("",         "女");
        $header["GOUKAKU_T"]        = array("",         "計");
        $header["NAITEI_M"]         = array("内定者",   "男");
        $header["NAITEI_F"]         = array("",         "女");
        $header["NAITEI_T"]         = array("",         "計");

        //ヘッダ（2行目、3行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header1[] = $val[0];
            $header2[] = $val[1];

            $header_array[] = $key;
        }

        //各行数取得
        $cntData = $db->getRow(knje434Query::getCsvQuery($this, "cnt"), DB_FETCHMODE_ASSOC);

        //SQL文発行
        $data = false;
        $dataTemp = array();
        $subtotal = $total = array();
        $seq = $seq_pref = "";
        $subcounter = $counter = 0;
        $query = knje434Query::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            if ($seq != $row["SEQ"]) {
                //初期化
                $subtotal = array();
                $subcounter = 0;

                //ヘッダーセット
                $contents .= implode($header1, ",") ."\n";
                $contents .= implode($header2, ",") ."\n";
            }

            if ($seq == $row["SEQ"]) $row["SEQ_NAME"] = "";
            if ($seq_pref == $row["SEQ"].$row["PREF_CD"]) $row["PREF_NAME"] = "";

            $sep = "";
            foreach ($header_array as $hkey) {
                //データセット
                $contents .= $sep.$row[$hkey];
                $sep = ",";

                if (preg_match('/^GOUKAKU/', $hkey) || preg_match('/^NAITEI/', $hkey)) {
                    $subtotal[$hkey] += $row[$hkey];
                    $total[$hkey] += $row[$hkey];
                } else {
                    $subtotal[$hkey] = ($hkey == "COMPANY_NAME") ? '計' : "";
                    $total[$hkey] = ($hkey == "COMPANY_NAME") ? '総合計' : "";
                }
            }
            $contents .= "\n";

            $subcounter++;
            $counter++;

            //小計
            if ($subcounter == $cntData["SEQ".$row["SEQ"]]) {
                $sep = "";
                foreach ($header_array as $hkey) {
                    //データセット
                    $contents .= $sep.$subtotal[$hkey];
                    $sep = ",";
                }
                $contents .= "\n\n";
            }

            //総合計
            if ($counter == $cntData["TOTAL"]) {
                $sep = "";
                foreach ($header_array as $hkey) {
                    //データセット
                    $contents .= $sep.$total[$hkey];
                    $sep = ",";
                }
                $contents .= "\n";
            }

            $seq        = $row["SEQ"];
            $seq_pref   = $row["SEQ"].$row["PREF_CD"];
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = CTRL_YEAR."年度_就職状況集計表";
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
