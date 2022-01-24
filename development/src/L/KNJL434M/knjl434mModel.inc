<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl434mModel extends Model
{
    public $field = array();
    public $cmd;
    public $examYear;

    /**
     * メンバ変数の初期化
     */
    public function knjl434mModel()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"              => VARS::post("YEAR"),             //年度
                             "DATE"              => VARS::post("DATE"),             //調査日付
                             "OUTPUT"            => VARS::post("OUTPUT"),           //フォーム選択
                             "DOCUMENTROOT"      => VARS::post("DOCUMENTROOT"),
                             "DBNAME"            => VARS::post("DBNAME"),           //DB変更に備えて
                             "EXAM_SELECTED_KEY" => VARS::post("EXAM_SELECTED_KEY"),
                             "SCHOOL_KIND_DIV"   => VARS::post("SCHOOL_KIND_DIV"),
                             "EXAM_NAME"         => VARS::post("EXAM_NAME"),
                             "EXAM_SELECTED"     => VARS::post("EXAM_SELECTED")
                     );

        $this->examYear = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $contents  = "";

        //DB接続
        $db = Query::dbCheckOut();

        $data = false;

        $keys = explode(",", $this->field["EXAM_SELECTED_KEY"]);
        if (!empty($keys)) {
            for ($ii = 0; get_count($keys) > $ii; $ii++) {
                $examKey = $keys[$ii];
                if ($examKey == "") {
                    continue;
                }
                // ヘッダ情報取得
                $query = knjl434mQuery::getExamHeader($this->examYear, $examKey);
                $result = $db->query($query);
                $row = $result->fetchRow(DB_FETCHMODE_ASSOC);

                $examDate = new DateTime($row["EXAM_DATE"]);

                // 学校名取得
                $schoolKind = substr($examKey, 0, 1);
                $certifKindCd = ($schoolKind == "H" ? "104" : ($schoolKind == "J" ? "105" : ""));
                $query = knjl434mQuery::getExamSchoolName($this->examYear, $certifKindCd);
                $result = $db->query($query);
                $row2 = $result->fetchRow(DB_FETCHMODE_ASSOC);

                // ヘッダ部成形
                $contents .= ",,,,合　　格　　発　　表,,,,,\r\n";
                $contents .= "下記の番号の方は、,," . $row["EXAM_NAME"] . ",,,(" . $examDate->format('m月d日') . $row["AM_PM"] . "実施)　に合格しました。,,,,\r\n";
                $contents .= "　入学手続きの書類をお受け取りください。,,,,,,," . $row2["SCHOOL_NAME"] . ",,\r\n";
                $contents .= "\r\n";

                // 明細形成
                $receptNos = array();
                $query = knjl434mQuery::getRecept($this->examYear, $examKey);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $receptNos[] = $row["RECEPTNO"];
                }

                $x = 1;
                $y = 1;
                $maxRow = 10;

                $maxLine = ceil(get_count($receptNos) / $maxRow);

                // 出力受験番号を縦に並び替え
                $noList = array();
                for ($tt = 0; get_count($receptNos) > $tt; $tt++) {
                    $noList[$y][$x] = $receptNos[$tt];
                    if ($maxLine == $y) {
                        $y = 1;
                        $x++;
                    } else {
                        $y++;
                    }
                }

                // 明細作成
                for ($cntY = 1; get_count($noList) >= $cntY; $cntY++) {
                    $sep = "";
                    for ($cntX = 1; get_count($noList[$cntY]) >= $cntX; $cntX++) {
                        $contents .= $sep . $noList[$cntY][$cntX];
                        $sep = ",";
                    }
                    $contents .= "\r\n";
                }
                $contents .= "\r\n";

                $data = true;
            }
        }

        Query::dbCheckIn($db);

        if ($data) {
            $couse="ＨＰ掲載用合格者一覧";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
