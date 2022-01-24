<?php

require_once('for_php7.php');

class knjf140bModel extends Model {
    var $field    = array();
    var $data     = array();
    var $cmd;
    var $control; //コントロールマスタ
    var $staffcd; //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjf140bModel() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"         =>  VARS::post("OUTPUT"),         //出力取込種別
                             "YEAR"           =>  VARS::post("YEAR"),           //年度一覧
                             "DATE"           =>  VARS::post("DATE"),           //健診実施日
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"), //年組一覧
                             "HEADER"         =>  VARS::post("HEADER"),         //ヘッダ有
                             "SHORI_MEI"      =>  VARS::post("SHORI_MEI")       //処理名
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->getPropertiesAll(); //プロパティファイル
    }

    function &getMainModel() {}

    //ファイル取り込み
    function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->getFileString();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjf140bQuery::deleteinsertQueryCsv($db, $this->dataArr[$i]);
                    //削除
                    } else {
                        knjf140bQuery::deleteQueryCsv($db, $this->dataArr[$i]);
                    }
                }
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    public function getFileString()
    {
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array( "YEAR"                  => array(4, "年度"),
                                "SCHREGNO"              => array(8, "学籍番号"),
                                "HEIGHT"                => array(5, "身長"),
                                "WEIGHT"                => array(5, "体重"),
                                "R_BAREVISION_MARK"     => array(3, "裸眼視力(右)"),
                                "L_BAREVISION_MARK"     => array(3, "裸眼視力(左)"),
                                "R_VISION_MARK"         => array(3, "矯正視力(右)"),
                                "L_VISION_MARK"         => array(3, "矯正視力(左)"),
                                "R_EAR"                 => array(2, "聴力(右)"),
                                "L_EAR"                 => array(2, "聴力(左)"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);
            $i = 0;
            $record_no = 1;            //行番号(初期値)
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('MEDEXAM_DET_DAT');
            $primaryKey2 = $this->getPrimaryKey('MEDEXAM_HDAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                     //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("YEAR"              => CTRL_YEAR,
                                  "DATE"              => str_replace("/", "-", $this->field["DATE"]),
                                  "SCHREGNO"          => trim($tmp[0]),
                                  "HEIGHT"            => trim($tmp[12]),
                                  "WEIGHT"            => trim($tmp[13]),
                                  "R_BAREVISION_MARK" => trim($tmp[22]),
                                  "L_BAREVISION_MARK" => trim($tmp[23]),
                                  "R_VISION_MARK"     => trim($tmp[25]),
                                  "L_VISION_MARK"     => trim($tmp[26]),
                                  "R_EAR"             => trim($tmp[38]),
                                  "L_EAR"             => trim($tmp[39]));
                    //０埋め
                    $data = $this->zero_ume($data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
                    }
                }
                $record_no++; //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";
        $checkNameMstCd = array("R_BAREVISION_MARK"   => "F017",
                                "L_BAREVISION_MARK"   => "F017",
                                "R_VISION_MARK"       => "F017",
                                "L_VISION_MARK"       => "F017",
                                "R_EAR"               => "F010",
                                "L_EAR"               => "F010",
                               );
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
             //更新
            if ($this->field["SHORI_MEI"] == "1") {
                //必須チェック
                if (in_array($key, array("SCHREGNO","DATE"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //数値チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                if (in_array($key, array("HEIGHT", "WEIGHT"))) {
                    if ($data[$key] != "" && !is_numeric(str_replace(".", "", $data[$key]))) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                //文字数チェック
                if (in_array($key, array("SCHREGNO", "HEIGHT", "WEIGHT")) && strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //値チェック
                if (in_array($key, array("HEIGHT", "WEIGHT"))) {
                    if ($data[$key] != "" && !$this->atai_check($data[$key], 3, 1)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
                //存在チェック（名称マスタ）
                if ($checkNameMstCd[$key] && $data[$key] != "") {
                    $query = knjf140bQuery::getExistNameMst($checkNameMstCd[$key], $data[$key]);
                    $mark = $db->getOne($query);
                    if (strlen($mark) < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."(".$checkNameMstCd[$key].")");
                    }
                }
            //削除
            } else {
                if (in_array($key, array("SCHREGNO"))) {
                    //必須チェック
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                    //数値チェック
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }

        if ($returnFlg) {
            if ($data["DATE"] != "" && !$this->date_rep($data["DATE"])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, "健診実施日");
            }

            //存在チェック(更新時のみ)
            if ($this->field["SHORI_MEI"] == "1") {
                if ($data["SCHREGNO"] != "") {
                    $query = knjf140bQuery::getRegdCheck($data);
                    if ($db->getOne($query) < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データ");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjf140bQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array("SCHREGNO"          => "※学籍番号",
                        "CODE1"             => "コード",
                        "CODE2"             => "年",
                        "CODE3"             => "組",
                        "CODE4"             => "番",
                        "CODE5"             => "氏名",
                        "CODE6"             => "性別",
                        "CODE7"             => "生年月日",
                        "CODE8"             => "年齢",
                        "CODE9"             => "学校名",
                        "CODE10"            => "学校長名",
                        "CODE11"            => "入力年度",
                        "HEIGHT"            => "身長",
                        "WEIGHT"            => "体重",
                        "CODE12"            => "BMI",
                        "CODE13"            => "BMI判定",
                        "CODE14"            => "a",
                        "CODE15"            => "b",
                        "CODE16"            => "身長別標準体重",
                        "CODE17"            => "肥満度",
                        "CODE18"            => "肥満度判定",
                        "CODE19"            => "メ・コの別",
                        "R_BAREVISION_MARK" => "裸眼視力(右)",
                        "L_BAREVISION_MARK" => "裸眼視力(左）",
                        "CODE20"            => "裸眼視力結果",
                        "R_VISION_MARK"     => "矯正視力(右)",
                        "L_VISION_MARK"     => "矯正視力(左)",
                        "CODE21"            => "矯正視力結果",
                        "CODE22"            => "視力結果",
                        "CODE23"            => "視力受診勧告",
                        "CODE24"            => "視力事後措置",
                        "CODE25"            => "視力受診済み",
                        "CODE26"            => "視力受診結果",
                        "CODE27"            => "眼の疾病及び異常",
                        "CODE28"            => "眼科受診勧告",
                        "CODE29"            => "眼科事後措置",
                        "CODE30"            => "眼科受診済み",
                        "CODE31"            => "眼科受診結果",
                        "R_EAR"             => "聴力(右)",
                        "L_EAR"             => "聴力(左）",
                        "CODE32"            => "聴力結果",
                        "CODE33"            => "聴力受診勧告",
                        "CODE34"            => "聴力事後措置",
                        "CODE35"            => "聴力受診済み",
                        "CODE36"            => "聴力受診結果",
                        "CODE37"            => "背骨チェック",
                        "CODE38"            => "腰チェック",
                        "CODE39"            => "肩・肘関節チェック",
                        "CODE40"            => "膝チェック",
                        "CODE41"            => "片脚立ちチェック",
                        "CODE42"            => "しゃがみこみチェック",
                        "CODE43"            => "その他の運動器",
                        "CODE44"            => "診断の有無",
                        "CODE45"            => "診断名",
                        "CODE46"            => "スポーツ",
                        "CODE47"            => "内科検診未検査",
                        "CODE48"            => "脊柱・胸郭・四肢",
                        "CODE49"            => "脊柱・胸郭・四肢受診勧告",
                        "CODE50"            => "脊柱・胸郭・四肢事後措置",
                        "CODE51"            => "整形外科受診済み",
                        "CODE52"            => "整形外科受診結果",
                        "CODE53"            => "栄養状態",
                        "CODE54"            => "栄養状態受診勧告",
                        "CODE55"            => "耳鼻咽頭疾患",
                        "CODE56"            => "耳鼻咽頭疾患受診勧告",
                        "CODE57"            => "皮膚疾患",
                        "CODE58"            => "皮膚疾患受診勧告",
                        "CODE59"            => "心臓疾病及び異常",
                        "CODE60"            => "心臓受診勧告",
                        "CODE61"            => "その他の疾病及び異常",
                        "CODE62"            => "その他の疾病及び異常受診勧告",
                        "CODE63"            => "内科検診結果",
                        "CODE64"            => "内科事後措置",
                        "CODE65"            => "内科受診済み",
                        "CODE66"            => "内科受診結果",
                        "CODE67"            => "結核間接撮影年月日",
                        "CODE68"            => "結核間接撮影所見",
                        "CODE69"            => "結核その他の検査",
                        "CODE70"            => "結核病名",
                        "CODE71"            => "結核指導区分",
                        "CODE72"            => "結核受診勧告",
                        "CODE73"            => "結核事後措置",
                        "CODE74"            => "結核受診済み",
                        "CODE75"            => "結核受診結果",
                        "CODE76"            => "心電図所見",
                        "CODE77"            => "心電図検査指示事項",
                        "CODE78"            => "心電図事後措置",
                        "CODE79"            => "心電図受診済み",
                        "CODE80"            => "心電図受診結果",
                        "CODE81"            => "尿一次蛋白",
                        "CODE82"            => "尿一次潜血",
                        "CODE83"            => "尿一次糖",
                        "CODE84"            => "尿一次備考",
                        "CODE85"            => "一次結果",
                        "CODE86"            => "尿二次蛋白",
                        "CODE87"            => "尿二次潜血",
                        "CODE88"            => "尿二次糖",
                        "CODE89"            => "尿二次備考",
                        "CODE90"            => "二次結果",
                        "CODE91"            => "尿受診勧告",
                        "CODE92"            => "尿事後措置",
                        "CODE93"            => "尿受診済み",
                        "CODE94"            => "尿受診結果",
                        "CODE95"            => "乳歯現在歯数",
                        "CODE96"            => "乳歯未処置歯有",
                        "CODE97"            => "乳歯未処置歯数",
                        "CODE98"            => "乳歯処置歯有",
                        "CODE99"            => "乳歯処置歯数",
                        "CODE100"           => "永久歯現在歯数",
                        "CODE101"           => "永久歯未処置歯有",
                        "CODE102"           => "永久歯未処置歯数",
                        "CODE103"           => "永久歯処置歯有",
                        "CODE104"           => "永久歯処置歯数",
                        "CODE105"           => "永久歯喪失歯数",
                        "CODE106"           => "顎関節",
                        "CODE107"           => "歯列・咬合",
                        "CODE108"           => "歯垢の状態",
                        "CODE109"           => "歯肉の状態",
                        "CODE110"           => "歯科その他の疾病及び異常",
                        "CODE111"           => "歯科所見",
                        "CODE112"           => "歯科検診結果",
                        "CODE113"           => "歯科検診結果２",
                        "CODE114"           => "歯科受診",
                        "CODE115"           => "歯科事後措置",
                        "CODE116"           => "歯科治療済み",
                        "CODE117"           => "歯科受診結果",
                        "CODE118"           => "備考",
                        "CODE119"           => "学校医所見",
                        "CODE120"           => "色覚希望",
                        "CODE121"           => "色覚結果",
                        "CODE122"           => "色覚受診勧告",
                        "CODE123"           => "色覚事後措置",
                        "CODE124"           => "色覚受診済み",
                        "CODE125"           => "色覚受診結果",
                        "CODE126"           => "視力　配布日",
                        "CODE127"           => "眼科　配布日",
                        "CODE128"           => "聴力　配布日",
                        "CODE129"           => "結核　配布日",
                        "CODE130"           => "心電図　配布日",
                        "CODE131"           => "内科　配布日",
                        "CODE132"           => "尿検査　配布日",
                        "CODE133"           => "歯科　配布日",
                        "CODE134"           => "色覚　配布日",
                        "CODE135"           => "検診結果　配布日");

        $header_rei = array("SCHREGNO"          => "99999999",
                            "CODE1"             => "コード",
                            "CODE2"             => "年",
                            "CODE3"             => "組",
                            "CODE4"             => "番",
                            "CODE5"             => "氏名",
                            "CODE6"             => "性別",
                            "CODE7"             => "生年月日",
                            "CODE8"             => "年齢",
                            "CODE9"             => "学校名",
                            "CODE10"            => "学校長名",
                            "CODE11"            => "入力年度",
                            "HEIGHT"            => "165.5",
                            "WEIGHT"            => "60.0",
                            "CODE12"            => "BMI",
                            "CODE13"            => "BMI判定",
                            "CODE14"            => "a",
                            "CODE15"            => "b",
                            "CODE16"            => "身長別標準体重",
                            "CODE17"            => "肥満度",
                            "CODE18"            => "肥満度判定",
                            "CODE19"            => "メ・コの別",
                            "R_BAREVISION_MARK" => "Ａ",
                            "L_BAREVISION_MARK" => "Ａ",
                            "CODE20"            => "裸眼視力結果",
                            "R_VISION_MARK"     => "Ａ",
                            "L_VISION_MARK"     => "Ａ",
                            "CODE21"            => "矯正視力結果",
                            "CODE22"            => "視力結果",
                            "CODE23"            => "視力受診勧告",
                            "CODE24"            => "視力事後措置",
                            "CODE25"            => "視力受診済み",
                            "CODE26"            => "視力受診結果",
                            "CODE27"            => "眼の疾病及び異常",
                            "CODE28"            => "眼科受診勧告",
                            "CODE29"            => "眼科事後措置",
                            "CODE30"            => "眼科受診済み",
                            "CODE31"            => "眼科受診結果",
                            "R_EAR"             => "／",
                            "L_EAR"             => "〇",
                            "CODE32"            => "聴力結果",
                            "CODE33"            => "聴力受診勧告",
                            "CODE34"            => "聴力事後措置",
                            "CODE35"            => "聴力受診済み",
                            "CODE36"            => "聴力受診結果",
                            "CODE37"            => "背骨チェック",
                            "CODE38"            => "腰チェック",
                            "CODE39"            => "肩・肘関節チェック",
                            "CODE40"            => "膝チェック",
                            "CODE41"            => "片脚立ちチェック",
                            "CODE42"            => "しゃがみこみチェック",
                            "CODE43"            => "その他の運動器",
                            "CODE44"            => "診断の有無",
                            "CODE45"            => "診断名",
                            "CODE46"            => "スポーツ",
                            "CODE47"            => "内科検診未検査",
                            "CODE48"            => "脊柱・胸郭・四肢",
                            "CODE49"            => "脊柱・胸郭・四肢受診勧告",
                            "CODE50"            => "脊柱・胸郭・四肢事後措置",
                            "CODE51"            => "整形外科受診済み",
                            "CODE52"            => "整形外科受診結果",
                            "CODE53"            => "栄養状態",
                            "CODE54"            => "栄養状態受診勧告",
                            "CODE55"            => "耳鼻咽頭疾患",
                            "CODE56"            => "耳鼻咽頭疾患受診勧告",
                            "CODE57"            => "皮膚疾患",
                            "CODE58"            => "皮膚疾患受診勧告",
                            "CODE59"            => "心臓疾病及び異常",
                            "CODE60"            => "心臓受診勧告",
                            "CODE61"            => "その他の疾病及び異常",
                            "CODE62"            => "その他の疾病及び異常受診勧告",
                            "CODE63"            => "内科検診結果",
                            "CODE64"            => "内科事後措置",
                            "CODE65"            => "内科受診済み",
                            "CODE66"            => "内科受診結果",
                            "CODE67"            => "結核間接撮影年月日",
                            "CODE68"            => "結核間接撮影所見",
                            "CODE69"            => "結核その他の検査",
                            "CODE70"            => "結核病名",
                            "CODE71"            => "結核指導区分",
                            "CODE72"            => "結核受診勧告",
                            "CODE73"            => "結核事後措置",
                            "CODE74"            => "結核受診済み",
                            "CODE75"            => "結核受診結果",
                            "CODE76"            => "心電図所見",
                            "CODE77"            => "心電図検査指示事項",
                            "CODE78"            => "心電図事後措置",
                            "CODE79"            => "心電図受診済み",
                            "CODE80"            => "心電図受診結果",
                            "CODE81"            => "尿一次蛋白",
                            "CODE82"            => "尿一次潜血",
                            "CODE83"            => "尿一次糖",
                            "CODE84"            => "尿一次備考",
                            "CODE85"            => "一次結果",
                            "CODE86"            => "尿二次蛋白",
                            "CODE87"            => "尿二次潜血",
                            "CODE88"            => "尿二次糖",
                            "CODE89"            => "尿二次備考",
                            "CODE90"            => "二次結果",
                            "CODE91"            => "尿受診勧告",
                            "CODE92"            => "尿事後措置",
                            "CODE93"            => "尿受診済み",
                            "CODE94"            => "尿受診結果",
                            "CODE95"            => "乳歯現在歯数",
                            "CODE96"            => "乳歯未処置歯有",
                            "CODE97"            => "乳歯未処置歯数",
                            "CODE98"            => "乳歯処置歯有",
                            "CODE99"            => "乳歯処置歯数",
                            "CODE100"           => "永久歯現在歯数",
                            "CODE101"           => "永久歯未処置歯有",
                            "CODE102"           => "永久歯未処置歯数",
                            "CODE103"           => "永久歯処置歯有",
                            "CODE104"           => "永久歯処置歯数",
                            "CODE105"           => "永久歯喪失歯数",
                            "CODE106"           => "顎関節",
                            "CODE107"           => "歯列・咬合",
                            "CODE108"           => "歯垢の状態",
                            "CODE109"           => "歯肉の状態",
                            "CODE110"           => "歯科その他の疾病及び異常",
                            "CODE111"           => "歯科所見",
                            "CODE112"           => "歯科検診結果",
                            "CODE113"           => "歯科検診結果２",
                            "CODE114"           => "歯科受診",
                            "CODE115"           => "歯科事後措置",
                            "CODE116"           => "歯科治療済み",
                            "CODE117"           => "歯科受診結果",
                            "CODE118"           => "備考",
                            "CODE119"           => "学校医所見",
                            "CODE120"           => "色覚希望",
                            "CODE121"           => "色覚結果",
                            "CODE122"           => "色覚受診勧告",
                            "CODE123"           => "色覚事後措置",
                            "CODE124"           => "色覚受診済み",
                            "CODE125"           => "色覚受診結果",
                            "CODE126"           => "視力　配布日",
                            "CODE127"           => "眼科　配布日",
                            "CODE128"           => "聴力　配布日",
                            "CODE129"           => "結核　配布日",
                            "CODE130"           => "心電図　配布日",
                            "CODE131"           => "内科　配布日",
                            "CODE132"           => "尿検査　配布日",
                            "CODE133"           => "歯科　配布日",
                            "CODE134"           => "色覚　配布日",
                            "CODE135"           => "検診結果　配布日");

        $contents  = ""; //クリア

        if($this->field["OUTPUT"]=="1") { //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {        //エラー
            $duplicateHeader = array("YEAR"          => "年度",
                                     "SCHREGNO"      => "学籍番号");
            $tables = array();
            $tables = array("1" => "MEDEXAM_DET_DAT", "2" => "MEDEXAM_HDAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } else if($this->field["OUTPUT"]=="4") {        //データ
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }
        if($this->field["OUTPUT"]=="1") { //ヘッダ
            $header_data = true;
        } else { //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->field["OUTPUT"] == "3") { //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if($this->field["OUTPUT"]=="4") { //データ
                $query = knjf140bQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if($this->field["OUTPUT"]=="4") { //データ
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if($header_data) {
            if($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "健康診断（らくだZOO）";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //値チェック
    function atai_check($data, $atai1, $atai2)
    {
        $flg = true;

        if ($data != "") {
            $data_arr = array();
            $data_arr = explode(".", $data);
            if (strlen($data_arr[0]) > $atai1) $flg = false;
            if (strlen($data_arr[1]) > $atai2) $flg = false;
        }

        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    function zero_ume($data)
    {
        if ($data["SCHREGNO"] != "") {
            $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
?>
