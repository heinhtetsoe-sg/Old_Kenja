<?php

require_once('for_php7.php');

class knjd627cModel extends Model
{
    public $programID;
    public $cmd;
    public $year;
    public $grade;
    public $target_subject;
    public $target_grade;
    public $selected_classes;
    public $term;
    public $borderscore;
    public $eventFrom;  //ポスト発行元コントロール名
    public $classcd;
    public $curriculumcd;
    public $subclasscd;
    public $retry_test_year;             //再試験対象年度
    public $retry_test_semester  = "9";  //再試験対象学期
    public $retry_test_kind      = "99"; //再試験対象テスト種別
    public $retry_test_itemcd    = "00"; //再試験対象テスト項目コード
    public $retry_test_score_div = "08"; //再試験対象採点区分

    public function init()
    {
        $this->year              = CTRL_YEAR;
        $this->cmd               = VARS::request("cmd");
        $this->grade             = VARS::post("GRADE");
        $this->target_grade      = VARS::post("TARGET_GRADE");
        $this->target_subject    = VARS::post("TARGET_SUBJECT");
        $this->selected_classes  = VARS::post("HID_CLASS_SELECTED");
        $this->term              = VARS::post("TERM");
        $this->borderscore       = VARS::post("BORDER_SCORE");
        $this->eventFrom         = VARS::post("HID_EVENT_FROM");

        if (isset($this->borderscore) == false) {
            $this->borderscore = "54";
        }

        unset($this->classcd);
        unset($this->curriculumcd);
        unset($this->subclasscd);
        if (isset($this->target_subject)) {
            $splited = explode("-", $this->target_subject);
            if (is_array($splited) && 2 < get_count($splited)) {
                $this->classcd      = $splited[0];
                $this->curriculumcd = $splited[1];
                $this->subclasscd   = $splited[2];
            }
        }

        $this->selectSchoolKind = VARS::get("URL_SCHOOLKIND");
        $this->getPropertiesAll(); //プロパティファイル読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //選択済みクラス一覧
        if ($this->eventFrom == "GRADE") {//現在の学年に変更あり
            $this->selected_classes = "";//選択済みクラスを初期化
        } else {
            if (isset($this->selected_classes)) {
                //選択済みクラスを配列化
                $this->selected_classes = explode(",", $this->selected_classes);
            }
        }
    }

    //学校種別の指定有無を取得
    public function isIndicateSchoolKind()
    {
        return ($this->selectSchoolKind != "");
    }

    public function getDownloadModel()
    {
        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ";
        }
        $queryParams = array(
            'cmd'             => 'csv',
            'GRADE'           => $this->grade,
            'TARGET_GRADE'    => $this->target_grade,
            'TARGET_SUBJECT'  => $this->target_subject,
            'CLASS_SELECTED'  => implode(",", $this->selected_classes),
            'TERM'            => $this->term,
            'BORDER_SCORE'    => $this->borderscore,
            'PRGID'           => $this->programID,
            'HID_YEAR'        => CTRL_YEAR,
            'HID_SEMESTER'    => CTRL_SEMESTER,
            'LOGIN_DATE'      => str_replace("-", "/", CTRL_DATE),
            'DBNAME'          => DB_DATABASE,
            'DOCUMENTROOT'    => DOCUMENTROOT,
            'SCHOOLCD'        => SCHOOLCD,
            'SCHOOLKIND'      => SCHOOLKIND,
        );
        foreach (array("useCurriculumcd",
                      "useSchool_KindField",
                      "use_prg_schoolkind",
                      "selectSchoolKind") as $name) {
            $queryParams[$name] = $this->Properties[$name];
        }

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($queryParams)
            )
        );
        $arr = array();
        $context  = stream_context_create($options);
        $queryContents = file_get_contents($url, false, $context);
        $title = "";
        $contents = "";

        if ($queryContents != false) {
            $map = json_decode($queryContents, true);
            $title = $map["TITLE"];
            foreach ($map["OUTPUT_LINES"] as $key => $csv) {
                $line = "";
                $comma = "";
                foreach ($csv as $col) {
                    $line .= $comma.$col;
                    $comma = ",";
                }
                $contents .= $line."\n";
            }
        }

        //CSV出力
        if ($contents) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
