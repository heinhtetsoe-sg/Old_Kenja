<?php

require_once('for_php7.php');

class knje370iModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knje370iModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array(
            "TARGET_DIV" => VARS::post("TARGET_DIV"),
        );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function getHyoteiHeikin($schregnoArray) {

        //$url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJE";
        $url = SERVLET_URL ."/KNJE";
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJE.KNJE370IHyoteiHeikin',
                      'METHOD_NAME'          => 'fetchHyoteiHeikin',
                      'DBNAME'               => DB_DATABASE,
                      'YEAR'                 => CTRL_YEAR,
                      'SEMESTER'             => CTRL_SEMESTER,
                      'HYOTEI'               => $this->field["HYOTEI"],
                      'category_selected'    => $schregnoArray,
                      'useCurriculumcd'      => $this->Properties["useCurriculumcd"],
                      'useProvFlg'           => $this->Properties["useProvFlg"]
        );

        $httpQuery = http_build_query($data);
        $httpQuery = preg_replace('/%5B(?:[0-9]|[1-9][0-9]+)%5D=/', '=', $httpQuery); // 配列インデクスをカット
        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => $httpQuery 
            )
        );
        $result = file_get_contents($url, false, stream_context_create($options));

        $arr = array();
        $arr["CLASSCD"] = array();
        $arr["HYOTEIHEIKIN"] = array();
        if ($result != FALSE) {
            $decoded = json_decode($result, true);
            foreach ($decoded as $schregno => $divs) {
                foreach (array("1", "9") as $div) { // 前期、後期
                    foreach ($divs[$div] as $hyoteiHeikin) {
                        $classcd = $hyoteiHeikin["CLASSCD"];
                        $arr["HYOTEIHEIKIN"][$schregno][$div][$classcd]["AVG"] = $hyoteiHeikin["AVG"];
                        $arr["HYOTEIHEIKIN"][$schregno][$div][$classcd]["GAIHYO"] = $hyoteiHeikin["GAIHYO"];
                        if ($div == "1" && $classcd != "TOTAL" && !in_array($classcd, $arr["CLASSCD"])) {
                            $arr["CLASSCD"][] = $classcd;
                        }
                    }
                }
            }
        }
        return $arr;
    }

    //CSV出力処理
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル）
        $title = (CTRL_YEAR+1)."年入試結果";
        $contents .= $title. str_repeat(",", 10). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";

        //初期化
        $header = array();

        //ヘッダ
        $header["YEAR"]                   = array("", "入試年度");
        // $header["OLDSCHREGTYPE"]          = array("", "現役・過年度正区分");
        $header["GRD_DATE"]               = array("", "卒業年度");
        $header["INOUTCD_NAME"]           = array("", "内進生・高入生区分");
        $header["HR_CLASS_NAME"]          = array("", "年組番");
        $header["NAME_SHOW"]              = array("", "氏名");
        $header["NAME_KANA"]              = array("", "氏名ひらがな");

        $header["FST_RATE_AVE"]           = array("", "高等部３年前期時点の評定平均値");
        $header["FST_RANK"]               = array("", "高等部３年前期時点の評定平均値 概評");
        $header["END_RATE_AVE"]           = array("", "高等部３年学年末時点の評定平均値");
        $header["END_RANK"]               = array("", "高等部３年学年末時点の評定平均値 概評");
        $header["COURSECODEABBV1"]        = array("", "玉聖コース");

        $header["SCHOOL_CD"]              = array("", "大学コード");
        $header["FACULTYCD"]              = array("", "学部コード");
        $header["DEPARTMENTCD"]           = array("", "学科コード");
        $header["PROGRAM_CD"]             = array("", "日程コード");
        $header["FORM_CD"]                = array("", "入試方式コード");
        $header["S_CD"]                   = array("", "系統コード（小分類）");
        $header["SCHOOL_NAME"]            = array("", "大学名");
        $header["FACULTYNAME"]            = array("", "学部名");
        $header["DEPARTMENTNAME"]         = array("", "学科名");
        $header["PROGRAM_NAME"]           = array("", "日程名");
        $header["FORM_NAME"]              = array("", "方式名");
        $header["S_NAME"]                 = array("", "系統名称（小分類）");
        $header["ADVERTISE_FLG"]          = array("", "募集単位フラグ");
        $header["BACHELOR_DIV"]           = array("", "文理区分");
        $header["PREF_CD"]                = array("", "県コード");
        $header["CENTER_PARTICIPATE"]     = array("", "センター採用区分");
        $header["WANTED_STUDENT_CNT"]     = array("", "募集人数");
        $header["JUDGE_DATE"]             = array("", "判定基準B");
        $header["CENTER_JUDGE_B"]         = array("", "センター判定B");
        $header["L_CD1"]                  = array("", "系統コード大分類1");
        $header["L_CD2"]                  = array("", "系統コード大分類2");
        $header["L_CD3"]                  = array("", "系統コード大分類3");
        $header["L_CD4"]                  = array("", "系統コード大分類4");
        $header["L_CD5"]                  = array("", "系統コード大分類5");
        $header["S_CD1"]                  = array("", "系統コード小分類1");
        $header["S_CD2"]                  = array("", "系統コード小分類2");
        $header["S_CD3"]                  = array("", "系統コード小分類3");
        $header["S_CD4"]                  = array("", "系統コード小分類4");
        $header["S_CD5"]                  = array("", "系統コード小分類5");
        $header["S_CD6"]                  = array("", "系統コード小分類6");
        $header["S_CD7"]                  = array("", "系統コード小分類7");
        $header["S_CD8"]                  = array("", "系統コード小分類8");
        $header["S_CD9"]                  = array("", "系統コード小分類9");
        $header["S_CD10"]                 = array("", "系統コード小分類10");
        $header["ADVERTISE_DIV"]          = array("", "募集区分");
        $header["LIMIT_DATE_WEB"]         = array("", "締切日（WEB）");
        $header["LIMIT_DATE_WINDOW"]      = array("", "締切日（窓口）");
        $header["LIMIT_DATE_MAIL"]        = array("", "締切日（郵便）");
        $header["LIMIT_DATE_MAIL2"]       = array("", "締切日（消印）");
        $header["EXAM_DATE"]              = array("", "入試日");
        $header["EXAM_PASS_DATE"]         = array("", "合格発表日");
        $header["PROCEDURE_LIMIT_DATE"]   = array("", "一般手続締切日");
        $header["ENT_MONEY"]              = array("", "納付金（入学時）");
        $header["PROCEDURE_MONEY"]        = array("", "納付金（手続時）");
        $header["TOTAL_MONEY"]            = array("", "納付金（初年度合格）");
        $header["ACCEPTANCE_CRITERION_A"] = array("", "判定基準A");
        $header["ACCEPTANCE_CRITERION_B"] = array("", "判定基準B");
        $header["ACCEPTANCE_CRITERION_C"] = array("", "判定基準C");
        $header["ACCEPTANCE_CRITERION_D"] = array("", "判定基準D");
        $header["DOCKING_CRITERION_A"]    = array("", "ドッキング判定基準A");
        $header["DOCKING_CRITERION_B"]    = array("", "ドッキング判定基準B");
        $header["DOCKING_CRITERION_C"]    = array("", "ドッキング判定基準C");
        $header["DOCKING_CRITERION_D"]    = array("", "ドッキング判定基準D");
        $header["CENTER_CRITERION_A"]     = array("", "センター総合判定基準A");
        $header["CENTER_CRITERION_B"]     = array("", "センター総合判定基準B");
        $header["CENTER_CRITERION_C"]     = array("", "センター総合判定基準C");
        $header["CENTER_CRITERION_D"]     = array("", "センター総合判定基準D");

        $header["SCHOOL_SORT"]            = array("", "学校類別コード");
        $header["SCHOOL_SORT_NAME"]       = array("", "学校類別");
        $header["SSAVG"]                  = array("", "SSAVG（大学の判定基準Bの平均）");
        $header["HOWTOEXAM"]              = array("", "受験方式コード");
        $header["HOWTOEXAM_NAME"]         = array("", "受験方式名");
        $header["DECISION"]               = array("", "受験結果コード");
        $header["DECISION_NAME"]          = array("", "受験結果");
        $header["PLANSTAT"]               = array("", "進路状況コード（入学ほか）");
        $header["PLANSTAT_NAME"]          = array("", "進路状況（入学ほか）");

        $header["SELECT_DIV"]             = array("", "選考分類コード（指定校推薦選考会議）");
        $header["SELECT_DIV_NAME"]        = array("", "選考分類（指定校推薦選考会議）");
        $header["SELECT_RESULT"]          = array("", "選考結果（指定校推薦選考会議）");
        $header["SELECT_RANK"]            = array("", "志望順位");
        $header["STAT_DATE1"]             = array("", "試験日");
        $header["STAT_DATE3"]             = array("", "発表日");


        //SQL文発行
        $schregnoArray = array();
        $dataTemp = array();
        $query = knje370iQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $keyStr = $row["SEQ"]."-".$row["SCHOOL_CD"]."-".$row["FACULTYCD"]."-".$row["DEPARTMENTCD"]."-".$row["PROGRAM_CD"]."-".$row["FORM_CD"]."-".$row["S_CD"]."-".$row["SCHREGNO"];
            if (!isset($dataTemp[$keyStr])) {
                $dataTemp[$keyStr] = $row;
            }
            if (!in_array($row["SCHREGNO"], $schregnoArray)) {
                $schregnoArray[] = $row["SCHREGNO"];
            }
        }
        $result->free();
        $hyoteiHeikinArray = knje370iModel::getHyoteiHeikin($schregnoArray);
        $classMstRows = $db->getAll(knje370iQuery::getClassMst($hyoteiHeikinArray["CLASSCD"]), array(), DB_FETCHMODE_ASSOC);

        foreach ($classMstRows as $classMst) {
            $header["SUBCLASS_".$classMst["CLASSCD"]] = array("", $classMst["CLASSNAME"]);
        }

        foreach ($dataTemp as $key => $val) {
            $schregno = $val["SCHREGNO"];
            $dataTemp[$key]["FST_RATE_AVE"] = $hyoteiHeikinArray["HYOTEIHEIKIN"][$schregno]["1"]["TOTAL"]["AVG"];
            $dataTemp[$key]["FST_RANK"]     = $hyoteiHeikinArray["HYOTEIHEIKIN"][$schregno]["1"]["TOTAL"]["GAIHYO"];
            $dataTemp[$key]["END_RATE_AVE"] = $hyoteiHeikinArray["HYOTEIHEIKIN"][$schregno]["9"]["TOTAL"]["AVG"];
            $dataTemp[$key]["END_RANK"]     = $hyoteiHeikinArray["HYOTEIHEIKIN"][$schregno]["9"]["TOTAL"]["GAIHYO"];
            foreach ($classMstRows as $classMst) {
                $classcd = $classMst["CLASSCD"];
                $dataTemp[$key]["SUBCLASS_".$classcd] = $hyoteiHeikinArray["HYOTEIHEIKIN"][$schregno]["1"][$classcd]["AVG"];
            }
        }

        //ヘッダ（2行目、3行目）セット
        $header1 = $header2 = $header_array = array();
        foreach ($header as $key => $val) {
            $header2[] = $val[1];
            $header_array[] = $key;
        }
        $contents .= implode($header2, ",") ."\n";

        $data = false;
        foreach ($dataTemp as $key => $val) {
            //ヘッダー順で出力
            $sep = "";
            foreach ($header_array as $hkey) {
                if($hkey == "SSAVG"){
                    $dataTemp[$key][$hkey] = knje370iModel::floattostr($dataTemp[$key][$hkey]);
                }

                //データセット
                $contents .= $sep.$dataTemp[$key][$hkey];
                $sep = ",";
            }

            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = "進路情報";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //小数点以降の末尾0をカット
    function floattostr($val) {
        preg_match( "#^([\+\-]|)([0-9]*)(\.([0-9]*?)|)(0*)$#", trim($val), $o );
        return $o[1].sprintf('%d',$o[2]).($o[3]!='.'?$o[3]:'');
    }
}
?>
