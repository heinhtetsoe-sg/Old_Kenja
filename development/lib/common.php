<?php

require_once('for_php7.php');

define('DATE_CALC_BEGIN_WEEKDAY', 0);
//権限チェック
define('DEF_NOAUTH',            0);     //権限なし
define('DEF_REFER_RESTRICT',    1);     //制限付き参照
define('DEF_REFERABLE',         2);     //参照
define('DEF_UPDATE_RESTRICT',   3);     //制限付き更新可
define('DEF_UPDATABLE',         4);     //更新可

require_once("Date/Calc.php");
//共通関数
class common{

    /*
        @(f)

        機能      : 時間割管理用クラス表示

        返り値    : 表示用に変換されたクラス

        引き数    : ARG1 - ClassData        - 対象クラスデータ
        　　　　    ARG2 - DispMode         - 表示モード(0:101*形式,
                                                1:"101,102,103"形式

        機能説明  : 時間割管理用クラス表示に変換する

        備考      :
    */

    function PubFncData2Print($ClassData, $DispMode=0)
    {
        if (strlen($ClassData) == 3){
            return substr($ClassData,0,1) ."-" .(int) substr($ClassData,1,2);
        }elseif (strlen($ClassData) > 3){
            $ClassData = trim(chunk_split($ClassData, 3, " "));
            $arr = explode(" ", $ClassData);
            if ($DispMode == 0){
                return $arr[0] ."*";
            }elseif ($DispMode == 1){
                $tmp = $sp = "";
                for ($i = 0; $i < get_count($arr); $i++){
                    $tmp .= $sp .common::PubFncData2Print($arr[$i]);
                    $sp = ",";
                }
                return $tmp;
            }
        }

    }
    /*
        機能      : マスタより指定データの取得
        返り値    : データ
        引き数    : SQL            - 必要なデータを取得できるＳＱＬ
        機能説明  : 各マスタより指定データの取得
        備考      : GetMasterData(SQL)
    */

    function GetMasterData($SQL)
    {
        $db = Query::dbCheckOut();
        $query = $SQL;
        $result = $db->query($query);

        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        return $row;
    }
    /*
        機能      : 英数字を漢数字に変換します。
        返り値    : 漢数字化されます。
        引き数    : lngOrgNUmeral      - 変換対象の英数字
                    intIndex           - 0:そのまま変換(99999→九九九九九)、1:桁を考慮して変換(99999→九万九千九百九十九)
    */
    function PubFncKnjNumeral($inOrgNumeral,$intIndex)
    {
        $num = array("〇","一","二","三","四","五","六","七","八","九");
        $position = array("","十","百","千","万","十","百","千","億","十","百","千","兆");
         $num_len = strlen($inOrgNumeral);
        if ($intIndex=="0") {
            for ($i=0;$i<$num_len;$i++) {
                $knjNum .= $num[substr($inOrgNumeral,$i,1)];
            }
        } elseif ($intIndex=="1") {
            for ($i=0;$i<$num_len;$i++) {

                if (substr($inOrgNumeral,$i,1)<>"0" && (substr($inOrgNumeral,$i,1) <> 1
                    || $i+1==$num_len || $num_len-$i==5 || $num_len-$i==9 || $num_len-$i==13)) {

                    $knjNum .= $num[substr($inOrgNumeral,$i,1)];
                    if ($num_len-$i>5 && $num_len-$i-1<9) {
                        $position_need=1;
                    }
                    if ($num_len-$i>9 && $num_len-$i-1<13) {
                        $position_need=2;
                    }
                    if ($num_len-$i>13) {
                        $position_need=3;
                    }
                }
                if (substr($inOrgNumeral,$i,1)<>"0" || ($position_need=1 && $num_len-$i==5)
                    || ($position_need=2 && $num_len-$i==9) || ($position_need=3 && $num_len-$i==13)) {
                    $knjNum .= $position[$num_len-$i-1];
                }
            }
        }
        return $knjNum;
    }
    /*
        機能      : 職員マスタより指定年度、職名コードの職員を取得(人数指定可・赴任日付の新しい順)
        返り値    : データ
        引き数    : DataYear           - 対象年度
                    JobCode            - 職名コード
                    NumberOfPersons    - 取得人数(0ならば、全員)
                    Demi               - 返値として文字列が必要ならば入れる区切り文字
                    Index              - 返す氏名を選択(省略可)
        機能説明  : 職員マスタより指定年度、職名コードの職員を取得(人数指定可・赴任日付の新しい順)
    */

    function GetTeachersData($DataYear,$JobCode,$NumberOfPersons,$Demi="0",$Index)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT a.STAFFCD,";
        if ($Index==0) {
            $query .= "   a.LNAME || ' ' || a.FNAME  as STAFFNAME";
        } elseif ($Index==1) {
            $query .= "   a.LNAME_SHOW || ' ' || a.LNAME_SHOW as STAFFNAME ";
        } elseif ($Index==2) {
            $query .= "   a.LNAME_ENG || ' ' || a.FNAME_ENG as STAFFNAME";
        }
        $query .= " FROM staff_mst a,staffyear_dat b ";
        $query .= " WHERE b.STAFFYEAR = '" .$DataYear. "'";
        $query .= " AND b.JOBNAMECD = '" .$JobCode. "'";
        $query .= " AND a.STAFFCD = b.STAFFCD ";

        $result = $db->query($query);
        if ($Demi=="0") {
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            return $row;
        } else {
            if ($Demi=="") {
                $sep = ",";
            } else {
                $sep = $Demi;
            }
            $i=0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if ($i==0) {
                    $strData =$row["STAFFNAME"];
                } else {
                    $strData =$strData.$sep.$row["STAFFNAME"];
                }
                $i++;
                if ($NumberOfPersons != "0"){
                    if ($i == $NumberOfPersons){
                        break;
                    }
                }
            }
            return $strData;
        }
        Query::dbCheckIn($db);
    }
    /*
        機能      : 郵便番号から住所を取得
        返り値    : 住所
        引き数    : ZIPCode       - 欲しい住所の郵便番号
        機能説明  : 新旧対応
        備考      : ZipCode2Address("901-11")
    */
    function ZipCode2Address($ZIPCode)
    {
         $db = Query::dbCheckOut();

        if (strlen($ZIPCode)==8) {
            $query  = " SELECT PREF || CITY || TOWN AS ADDRESS FROM zipcd_mst ";
            $query .= " WHERE NEW_ZIPCD = '" .$ZIPCode. "'";
        } else {
            $query  = " SELECT PREF || CITY || TOWN AS ADDRESS FROM zipcd_mst ";
            $query .= " WHERE OLD_ZIPCD = '" .$ZIPCode. "'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["ADDRESS"];

    }
    /*
        機能      : コード＆名称データの分離
        返り値    : コード、名称
        引き数    : Wk_Word           - コード＆名称の元データ
                    Wk_Data           - 分離後の名称収納用
        機能説明  : '11 名称'などのデータを、コードと名称に分離する。
        備考      : GetDivisionData(Wk_Word)
    */
    function GetDivisionData($Wk_Word)
    {
        if (preg_match("/(^[0-9]+)([^0-9]+$)/",$Wk_Word,$Wk_Data) ) {
            return $Wk_Data;
        } else {
            return;
        }

    /*    for ($idx_lp=0;$idx_lp < strlen($Wk_Word);$idx_lp++) {
            $wk_Str = substr($Wk_Word, $idx_lp, 1);

            if (preg_match("([0-9])",$wk_Str)) {
                $Wk_Data[0] .= $wk_Str;
            } else {
                $Wk_Data[1] .= $wk_Str;
            }

        }

        return $Wk_Data; */
    }
    /*
        @(f)

        機能      : 名称マスタより指定データの取得

        返り値    : データ

        引き数    : ARG1 - Namecd1       - 必要な名称区分
        　　　      ARG2 - Column         - 必要なカラム名(列名)
        　　　      ARG3 - Namecd2           - 名称コードを直に指定可。その場合は一件のみになる
        　　　      ARG4 - args            - OUTPUT配列
        　　　      ARG5 - Year            - 対象年度、省略すると年度無視。

        機能説明  : 名称マスタより指定データの取得

    */

    function GetNameMaster($Namecd1,$Column,$Namecd2, &$args, $Year= 0)
    {
        if ($Year > 0){
            $query  = "SELECT NAMECD1, NAMECD2, " .$Column ;
            $query .= "  FROM V_NAME_MST ";
            $query .= " WHERE YEAR    = '" .$Year ."'";
            $query .= "   AND NAMECD1 = '" .$Namecd1 ."'".((strlen($Namecd2) > 0)? " AND NAMECD2 = '$Namecd2'":"");
        } else {
            $query = "SELECT NAMECD1, NAMECD2," .$Column ." FROM NAME_MST WHERE NAMECD1 = '" .$Namecd1."'" .((strlen($Namecd1) > 0)? " AND NAMECD2 = '$Namecd2'":"");
        }
        $db = Query::dbCheckOut();
        $args = $db->getAll($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return true;
    }

    /*
    機能      : 最新の年度を取得
    返り値    : 最新の年度
    引き数    : なし
    機能説明  : コントロールマスタより、最新の年度を取得
    備考      : GetSchoolYear
    */
    function GetSchoolYear()
    {
        $db = Query::dbCheckOut();

        $query .= "select CTRL_CHAR1 from control_mst ";
        $query .= "where CTRL_CD1 =  'Z001' and CTRL_CD2 = '0000' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["CTRL_CHAR1"];
    }


    /*
     @(f)
     
     機能      : 和暦リスト取得
     
     返り値    : 取得した和暦のリスト
     
     引き数    : なし
     
     機能説明  : 名称マスタより和暦のリストを取得する
     
     備考      :
          和暦のリスト
           list[0]['CD']    = '1'
           list[0]['Name']  = '明治'
           list[0]['SName'] = 'M'
           list[0]['Start'] = '1868/09/09'
           list[0]['End']   = '1912/07/29'
           list[0]['YearStart'] = '1868'
           list[0]['YearEnd']   = '1912'
             :
           list[3]['CD']    = '4'
           list[3]['Name']  = '平成'
           list[3]['SName'] = 'H'
           list[3]['Start'] = '1989/01/08'
           list[3]['End']   = '2019/04/30'
           list[0]['YearStart'] = '1989'
           list[0]['YearEnd']   = '2018'
             :
           list[n]['CD']    = '9'
           list[n]['Name']  = '〇〇'  ←新元号
           list[n]['SName'] = 'X'
           list[n]['Start'] = 'XXXX/XX/XX'
           list[n]['End']   = '9999/XX/XX'
           list[0]['YearStart'] = 'XXXX'
           list[0]['YearEnd']   = '9999'
     */
    function getWarekiList() {

        $warekiList = array();
        if (get_count($warekiList) <= 0) {
            $db = Query::dbCheckOut();

            $query .= "SELECT ";
            $query .= "  NAMECD1 ";
            $query .= "  , NAMECD2 ";
            $query .= "  , NAME1 ";
            $query .= "  , ABBV1 ";
            $query .= "  , ABBV3 ";
            $query .= "  , NAMESPARE1 ";
            $query .= "  , NAMESPARE2 ";
            $query .= "  , NAMESPARE3 ";
            $query .= "FROM DB2INST1.NAME_MST ";
            $query .= "WHERE NAMECD1 =  'L007' ";
            $query .= "ORDER BY NAMECD2 ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $warekiList[] = array(
                    "CD" => $row['NAMECD2'],
                    "Name" => $row['NAME1'],
                    "SName" => $row['ABBV1'],
                    "Start" => $row['NAMESPARE2'],
                    "End" => $row['NAMESPARE3'],
                    "YearStart" => $row['NAMESPARE1'],
                    "YearEnd" => $row['ABBV3'],
                );
            }
            $result->free();
            // DBの切断はココでは行わない（呼出元でDB接続使用している可能性がある）
            // セッションが切れるタイミングでDBも切断される
        }

        return $warekiList;
    }
    /*
        @(f)

        機能      : 和暦取得

        返り値    : 変換された日付、またはエラー

        引き数    : ARG1 - $year    - 年
                    ARG2 - $month   - 月
                    ARG3 - $day     - 日
        機能説明  : 日付を和暦に変換する

        備考      :
    */
    function Calc_Wareki(&$year, $month, $day)
    {
        $border = array();

        $warekiList = array();
        $warekiList = common::getWarekiList();

        for ($i = 0; $i < get_count($warekiList); $i++) {
            $warekiInfo = $warekiList[$i];
            $start = str_replace("/", "", $warekiInfo['Start']);
            $end = str_replace("/", "", $warekiInfo['End']);
            $border[] = array("開始日" =>  $start, "終了日" => $end, "元号" => $warekiInfo['Name']);
        }

        $target = sprintf("%04d%02d%02d", $year, $month, $day);
        for ($i = 0; $border[$i]; $i++){
            if ($border[$i]["開始日"] <= $target &&
                $target <= $border[$i]["終了日"] ){
                $year = ($year - substr($border[$i]["開始日"], 0, 4) + 1);
                return $border[$i]["元号"] .(($year == 1)? "元年" : sprintf("%2d", (int) $year)."年");
            }

        }
        return false;
    }
    /*
        @(f)

        機能      : 日付変換

        返り値    : 変換された日付、またはエラー

        引き数    : ARG1 - $OrgDate    - 変換元日付
        　　　      ARG2 - $Index      - 変換パターン

        機能説明  : 日付を特定のパターンに変換する

        備考      : DateConv1(Date, Index)
    */
    function DateConv1($OrgDate, $Index)
    {
        $wday = array("日","月","火","水","木","金","土");

        if (preg_match("/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2})/", $OrgDate, $regs)){
            $year   = $regs[1];
            $month  = $regs[2];
            $day    = $regs[3];
            if (!Date_Calc::isValidDate($day, $month, $year)){
                return false;
            }
            switch($Index){
                case 0: //"平成_9年_9月_7日"バージョン
                    return sprintf("%s%2d月%2d日", common::Calc_Wareki($year, $month, $day), $month, $day);
                case 1: //"平成_9年_9月"バージョン
                    return sprintf("%s%2d月", common::Calc_Wareki($year, $month, $day), $month);
                case 2: //"平成_9年"バージョン
                    return common::Calc_Wareki($year, $month, $day);
                case 3: //"平成_9年_9月_7日（月）"バージョン
                    $w = Date_Calc::dateFormat($day,$month,$year, "%w");
                    return sprintf("%s%2d月%2d日 (%s)",
                        common::Calc_Wareki($year, $month, $day), $month, $day, $wday[$w]);
                case 4: //"1997/09/07"バージョン
                    return sprintf("%04d/%02d/%02d", $year, $month, $day);
                case 5: //"1997/09"バージョン
                    return sprintf("%04d/%02d", $year, $month);
                case 6: //"1997"バージョン
                    return sprintf("%04d", $year);
                case 7: //"平成＿＿九年＿九月＿＿七日"バージョン
                case 10: //"平成_9年度"年度バージョン
                    $y = Date_Calc::daysToDate(Date_Calc::dateToDays($day,$month-3,$year), "%Y");
                    return common::Calc_Wareki($y, $month, $day) ."度";

                case 11: //"1999年度"年度バージョン
                    return Date_Calc::daysToDate(Date_Calc::dateToDays($day,$month-3,$year), "%Y") ."年度";
                case 12: //"1999"年度バージョン
                    return Date_Calc::daysToDate(Date_Calc::dateToDays($day,$month-3,$year), "%Y");
                default:
                    return false;
            }


        }else{
            return false;
        }

    }
    /*
        @(f)

        機能      : 日付変換２

        返り値    : 変換された日付、またはエラー

        引き数    : ARG1 - OrgDate    - 変換元日付
        　　　      ARG2 - OutDate1   - 変換先日付１
        　　　      ARG3 - OutDate2   - 変換先日付２
        　　　      ARG4 - Index      - 変換パターン

        機能説明  : 日付を特定のパターンに変換する

        備考      : DateConv2(OrgDate, OutDate1, OutDate2, Index)
    */
    function DateConv2($OrgDate,&$OutDate1,&$OutDate2,$Index)
    {
        if (preg_match("/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2})/", $OrgDate, $regs)){
            $year   = (int)$regs[1];
            $month  = (int)$regs[2];
            $day    = (int)$regs[3];
            if (!Date_Calc::isValidDate($day, $month, $year)){
                return false;
            }
            $w = Date_Calc::dateFormat($day,$month,$year, "%w");
            if($w == 0){    //日曜日ならば、
                $OutDate1 = Date_Calc::daysToDate(Date_Calc::dateToDays($day-6-$w,$month,$year), "%Y/%m/%d");
            }else{
                $OutDate1 = Date_Calc::daysToDate(Date_Calc::dateToDays($day+1-$w,$month,$year), "%Y/%m/%d");                               }
            if (preg_match("/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2})/", $OutDate1, $regs)){
                $year   = (int)$regs[1];
                $month  = (int)$regs[2];
                $day    = (int)$regs[3];
                switch($Index){
                    case 0: //指定週の開始日(月曜日)と終了日(日曜日)を取得
                        $OutDate2 = Date_Calc::daysToDate(Date_Calc::dateToDays($day+6,$month,$year), "%Y/%m/%d");
                        break;
                    case 1: //指定週の開始日(月曜日)と終了日(土曜日)を取得
                        $OutDate2 = Date_Calc::daysToDate(Date_Calc::dateToDays($day+5,$month,$year), "%Y/%m/%d");
                        break;
                    default:
                        return false;
                }
            }else{
                return false;
            }
        }
    }
    /*
        @(f)

        機能      : 警告メッセージ表示

        返り値    : Javascript文字列

        引き数    : ARG1 - msg - 警告メッセージ

        機能説明  : JavascriptのHTML文字列を作成する

        備考      : alert($str)
    */
    function alert($msg)
    {
        $str .= "<script language='javascript'>\n";
        $str .= "   alert('$msg')\n";
        $str .= "</script>";
        echo $str;
    }
    //確認メッセージ
    function confirm($msg, $url_ok='index.php',$url_cancel='index.php')
    {
        $str = "<HTML><HEAD><TITLE></TITLE>\n";
        $str .= "<script language='javascript'>\n";
        $str .= "   if (confirm('$msg')){\n";
        $str .= "       location.href='$url_ok';\n";
        $str .= "   }else{\n";
        $str .= "       location.href='$url_cancel';\n";
        $str .= "   }\n";
        $str .= "</script>";
        echo $str .= "</HEAD><BODY></BODY></HTML>";
    }
    /*
        @(f)

        機能      : メッセージマスタからメッセージを取得

        返り値    : Javascript文字列

        引き数    : ARG1 - $code  - メッセージコード
        　　　      ARG2 - $msg   - 追加する文字列

        機能説明  : メッセージマスタからメッセージを取得しJavascript
                    HTML文字列に変換する。

        備考      : ShowMessage($code, $msg='')
    */
    function &ShowMessage($code, $msg='')
    {
        $db = Query::dbCheckOut();

        $query = "SELECT MSG_CONTENT FROM MESSAGE_MST"
                ." WHERE MSG_CD  = '" .$code ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        $row["MSG_CONTENT"] = str_replace("\r\n", "\\n",$row["MSG_CONTENT"]);
        common::alert($row["MSG_CONTENT"] ."\\n" .$msg);
    }

    /*
        @(f)
        機能      : セキュリティチェック
        返り値    : ARG1 - SecurityCheck      - 0 権限無し
        　　　                                  1 参照のみ
        　　　                                  2 更新可
        　　　                                  3 制限付き参照
        　　　                                  4 制限付き更新可

        引き数    : $staffcd    ：職員コード
                    $appname    ：プログラムID
                    $year
        機能説明  : ユーザーＩＤ、アプリケーションごとの権限を取得できる
        備考      : なし
    */
    function SecurityCheck($staffcd, $appname)
    {
        
        //2017/07/10追加
        //SCHOOLKINDとSCHOOLCDを使うかの判断
        $properties["useSchool_KindMenu"] = "";

        $arr_useUnAdminMenuPrgid = array();
        $arr_useSubMenuId = array();
        $retVal = "";
        
        /*
        * configディレクトリ確認
        */
        if (file_exists(CONFDIR ."/menuInfo.properties")) {
            $filename = CONFDIR ."/menuInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/menuInfo.properties";
        }
        
        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            foreach ($properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $properties[$key] = str_replace("\r\n", "", $retVal);
                }
            }
        }
        fclose($fp);
        //2017/07/10追加ここまで
        
        
        $db = Query::dbCheckOut();
        //2017/07/10追加
        //上で取得したSCHOOLKINDとSCHOOLCDを使うかの分岐
        if($properties["useSchool_KindMenu"] == "1"){
            $query = "VALUES security_chk_prg('" .$staffcd ."','" .$appname ."','" .CTRL_YEAR  ."','" . SCHOOLKIND ."','" . SCHOOLCD ."')";
        } else {
            $query = "VALUES security_chk_prg('" .$staffcd ."','" .$appname ."','" .CTRL_YEAR  ."')";
        }
        
        $row = $db->getRow($query);
        Query::dbCheckIn($db);
        if (!is_array($row)){
            return 0;           //権限無し
        }else{
            switch($row[0]){
                case "0":       //更新可
                    return DEF_UPDATABLE;
                case "1":       //制限付き更新可
                    return DEF_UPDATE_RESTRICT;
                case "2":       //参照
                    return DEF_REFERABLE;
                case "3":       //制限付き参照
                    return DEF_REFER_RESTRICT;
                default:         //起動不可
                    return 0;
            }
        }
    }
    /*
        @(s)

        機能      : 学期の範囲をin_FiscalYearの年度に変更する

        返り値    : なし

        引き数    : ARG-1 in_FiscalYear 変更後の年度（西暦4桁）

        機能説明  : 学期の範囲をin_FiscalYearの年度に変更する

        備考      :
    */

    function ChangeFiscalYear_Sub($in_FiscalYear, &$args)
    {
        $d = getdate(strtotime($args["学期開始日付"][0]));
        $udate = mktime(0,0,0,$d["mon"]-3, $d["mday"], $d["year"]);
        $d = getdate($udate);
        $Wk_Year = (int)($in_FiscalYear) - (int)$d["year"];

        for($i = 0; $i <= 3; $i++){
            if (strtotime($args["学期開始日付"][$i])){
                $d = getdate(strtotime($args["学期開始日付"][$i]));
                $udate = mktime(0,0,0,$d["mon"], $d["mday"], (int)$d["year"]+$Wk_Year);
                $args["学期開始日付"][$i] = date("Y/m/d", $udate);
            }
            if (strtotime($args["学期終了日付"][$i])){
                $d = getdate(strtotime($args["学期終了日付"][$i]));
                $udate = mktime(0,0,0,$d["mon"], $d["mday"], (int)$d["year"]+$Wk_Year);
                $args["学期終了日付"][$i] = date("Y/m/d", $udate);
            }
        }
        return true;
    }

    /*
        @(s)

        機能      : 授業集数データから対象年度の勤怠集計開始日付、
                    勤怠集計終了日付を配列に代入

        返り値    : なし

        引き数    : ARG-1 年度
                    ARG-2 OUTPUT配列

        機能説明  : 授業集数データから対象年度の勤怠集計開始日付、
                    勤怠集計終了日付を配列に代入

        備考      :
    */
    function GetManagementTerm_Fnc($args, &$args2)
    {
        $query = "SELECT DISTINCT ";
        $query .= "SEMESTER,";
        $query .= "DATE(DI_SUM_SDATE),";
        $query .= "DATE(DI_SUM_FDATE) ";
        $query .= "FROM CLASSWEEK_DAT ";
        $query .= "WHERE YEAR = '" .$args["年度"] ."'";

        $db = Query::dbCheckOut();

        $result = $db->query($query);
        while( $row = $result->fetchRow()){
            $i = (int)$row[0] - 1;
            $args2["勤怠集計開始日付"][$i] = str_replace("-", "/", $row[1]);
            $args2["勤怠集計終了日付"][$i] = str_replace("-", "/", $row[2]);
        }
        if (is_array($args2)){
            $args2["勤怠集計開始日付"][3] = $args2["勤怠集計開始日付"][0];
            $args2["勤怠集計終了日付"][3] = $args2["勤怠集計終了日付"][$args["学期数"] - 1];
        }else{
            for ($i = 0; $i <= 3; $i++ ){
                $args2["勤怠集計開始日付"][$i] = $args["学期開始日付"][$i];
                $args2["勤怠集計終了日付"][$i] = $args["学期終了日付"][$i];
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    /*
        @(s)

        機能      : コントロールマスタよりコントロール日付１の取得

        返り値    : なし

        引き数    : ARG-1 OUTPUT配列

        機能説明  : コントロールマスタよりコントロール日付１の取得

        備考      :
    */
    function GetControlMaster_Fnc(&$args)
    {
        $db = Query::dbCheckOut();

        //CONTROL_MSTの読込
        $query  = " SELECT  ";
        $query .= " CTRL_NO,  ";
        $query .= " CTRL_YEAR,  ";          //学籍処理年度
        $query .= " CTRL_SEMESTER,  ";      //学籍処理学期
        $query .= " CTRL_DATE,  ";          //学籍処理日
        $query .= " IMAGEPATH,  ";          //
        $query .= " EXTENSION,  ";          //
        $query .= " MESSAGE,  ";            //
        $query .= " REGISTERCD,  ";         //
        $query .= " UPDATED ";              //
        $query .= " FROM CONTROL_MST ";
        $query .= " ORDER BY CTRL_NO";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //$args["証明日付"]    ;                                   //通知票の単位修得日付
        $args["年度"]           = (defined("CTRL_YEAR"))? CTRL_YEAR : $row["CTRL_YEAR"];
        $args["学期"]           = (defined("CTRL_SEMESTER"))? CTRL_SEMESTER : $row["CTRL_SEMESTER"];
        $args["学籍処理日"]     = (defined("CTRL_DATE"))? CTRL_DATE : $row["CTRL_DATE"];
        $args["LargePhotoPath"] = $row["IMAGEPATH"];
        $args["SmallPhotoPath"] = $row["IMAGEPATH"];
        $args["Extension"]      = $row["EXTENSION"];
        $args["MESSAGE"]        = $row["MESSAGE"];

        //学期情報の取得 SEMESTER_MST
        list($y, $s) = explode(",", VARS::post("CTRL_SEMESTER"));
        $setYear = $y ? $y : $args["年度"];
        $query = "";
        $query .= "SELECT ";
        $query .= "  YEAR, ";
        $query .= "  SEMESTER, ";
        $query .= "  SEMESTERNAME, ";
        $query .= "  SDATE, ";
        $query .= "  EDATE, ";
        $query .= "  REGISTERCD, ";
        $query .= "  UPDATED ";
        $query .= "FROM ";
        $query .= "  SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".$setYear."' ";

        //$row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $result = $db->query($query);
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $args["学期開始日付"][$row["SEMESTER"]] = str_replace("-", "/", $row["SDATE"]);
            $args["学期終了日付"][$row["SEMESTER"]] = str_replace("-", "/", $row["EDATE"]);
            $args["学期名"][$row["SEMESTER"]] = $row["SEMESTERNAME"];
            $args["SEMESTER"][$i] = $row["SEMESTER"];
            $i++;
        }

        //学期情報の取得 SCHOOL_MST
        $query = "";
        $query .= "SELECT ";
        $query .= "  YEAR, ";
        $query .= "  FOUNDEDYEAR, ";
        $query .= "  PRESENT_EST, ";
        $query .= "  SCHOOLNAME1, ";
        $query .= "  SCHOOLNAME2, ";
        $query .= "  SCHOOLNAME3, ";
        $query .= "  SCHOOLZIPCD, ";
        $query .= "  SCHOOLADDR1, ";
        $query .= "  SCHOOLADDR2, ";
        $query .= "  SCHOOLTELNO, ";
        $query .= "  SCHOOLFAXNO, ";
        $query .= "  SCHOOLMAIL, ";
        $query .= "  SCHOOLURL, ";
        $query .= "  SCHOOLDIV, ";
        $query .= "  SEMESTERDIV, ";
        $query .= "  GRADE_HVAL, ";
        $query .= "  ENTRANCE_DATE, ";
        $query .= "  GRADUATE_DATE, ";
        $query .= "  GRAD_CREDITS, ";
        $query .= "  SEMES_ASSESSCD, ";
        $query .= "  SEMES_FEARVAL, ";
        $query .= "  GRADE_FEARVAL, ";
        $query .= "  REGISTERCD ";
        $query .= "FROM ";
        $query .= "  SCHOOL_MST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".$setYear."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //$args["保留単位数"]   = $row[ ];     //対象項目無し 2003/07/09
        //$args["保留値学期"]     = $row["SEMESTERDIV"];  //？？？？？？？？
        //$args["保留値学年"]     = $row["GRADE_HVAL"];     //？？？？？？？？
        //$args["平均点補正係数"] = $row["TESTVALDIV"];     //？？？？？？？？
        //$args["出力制御日付"]   = $row[""];  //？？？？？？？？
        //$args["StampData"]    = $row[ ];     //対象項目無し 2003/07/09
        $args["学期末評価区分"] = $row["SEMES_ASSESSCD"];
        $args["学期数"]         = $row["SEMESTERDIV"];
        $args["学年数"]         = $row["GRADE_HVAL"];
        //$args["学期開始日付"][3] = $args["学期開始日付"][0]; //？？？？？？？？
        //$args["学期終了日付"][3] = $args["学期終了日付"][(int) $args["学期数"] - 1]; //？？？？？？？？
        $args["学校名1"]        = $row["SCHOOLNAME1"];
        $args["学校名2"]        = $row["SCHOOLNAME2"];
        $args["学校名3"]        = $row["SCHOOLNAME3"];
        $args["学校郵便番号"]   = $row["SCHOOLZIPCD"];
        $args["学校住所1"]      = $row["SCHOOLADDR1"];
        $args["学校住所2"]      = $row["SCHOOLADDR2"];
        $args["学校電話番号"]   = $row["SCHOOLTELNO"];
        $args["学校FAX番号"]    = $row["SCHOOLFAXNO"];
        $args["学校メールアドレス"] = $row["SCHOOLMAIL"];
        $args["学校HPアドレス"] = $row["SCHOOLURL"];
        $args["学校入学日"] = $row["ENTRANCE_DATE"];
        $args["学校卒業日"] = $row["GRADUATE_DATE"];
        $args["学校区分"] = $row["SCHOOLDIV"];


        //校長名の取得
        $query = "SELECT ";
        $query .= "   STAFFNAME_SHOW ";
        $query .= "FROM V_STAFF_MST ";
        $query .= "WHERE YEAR       = '" .$setYear."' ";
        $query .= "  AND STAFFCD    = '0001' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $args["校長氏名"] = $row["STAFFNAME_SHOW"];

        Query::dbCheckIn($db);

        return true;
    }

    /*

        @(f)

        機能      : データの抽出処理

        返り値    : True:成功、False:失敗

        引き数    : ARG-1 : $Mod 0:番号順、1:席次順

        機能説明  : データの抽出処理

        備考      :

    */

    function GetData_Fnc($Mod, $args, &$args2, $attendflg=false)
    {
        //対象者基本情報を求める
        $query = "SELECT DISTINCT ";
        $query .= "T1.SCHREGNO,";                                                    //0
        $query .= "T2.GRADE || '-' || T2.HR_CLASS  || '-' || T2.ATTENDNO, ";        //1
        $query .= "T3.NAME_SHOW, ";                                                 //2
        $query .= "T1.MOD_SCORE,";                                                  //3
        $query .= "T1.ATTEND_FLG ";                                                 //4
        $query .= "FROM TESTSCORE_DAT  T1,";
        $query .= "     SCHREG_REGD_DAT        T2,";
        $query .= "     SCHREG_BASE_MST        T3 ";
        $query .= "WHERE T1.YEAR            =   '" .$args["YEAR"] ."' ";
        $query .= "  AND T1.SEMESTER        =   '" .$args["SEMESTER"] ."' ";
        $query .= "  AND T1.TESTKINDCD      =   '" .$args["TESTKINDCD"] ."' ";
        $query .= "  AND T1.TESTITEMCD      =   '" .$args["TESTITEMCD"] ."' ";
        $query .= "  AND T1.CLASSCD         =   '" .$args["CLASSCD"] ."' ";
        $query .= "  AND T1.SUBCLASSCD      =   '" .$args["SUBCLASSCD"] ."' ";
        $query .= "  AND T1.ATTENDCLASSCD  IN  (" .$args["CurrentClassCode"] .") ";
        if ($attendflg){
            $query .= "  AND T1.ATTEND_FLG          =   '1' ";
        }
        $query .= "  AND T2.SCHREGNO          =   T1.SCHREGNO ";
        $query .= "  AND T2.YEAR              =   T1.YEAR ";
        $query .= "  AND T3.SCHREGNO          =   T1.SCHREGNO ";
        if( $Mod == 0 ){        //番号順
            $query .= "ORDER BY T2.GRADE || '-' || T2.HR_CLASS  || '-' || T2.ATTENDNO ";
        }else{                //席次順
            $query .= "ORDER BY T1.MOD_SCORE DESC, T2.GRADE || '-' || T2.HR_CLASS  || '-' || T2.ATTENDNO ";
        }
        $db = Query::dbCheckOut();

        $result = $db->query($query);
        $i = 0;
        while( $row = $result->fetchRow()){
            $args2[$row[0]]["StudentID"]    = $row[0];
            $args2[$row[0]]["SyusekiNo"]    = $row[1];
            $args2[$row[0]]["Name"]         = $row[2];
            $args2[$row[0]]["Score"]        = $row[3];
            $args2[$row[0]]["Flag"]         = ($row[4] = "0")? false : true;
            $args2[$row[0]]["Index"]        = $i++;

        }
        //人数,平均点、標準偏差を求める
        for($i = 0; $i <= 1; $i++){
            $query  = "SELECT ";
            $query .= "COUNT(MOD_SCORE), ";
            $query .= "STDDEV(MOD_SCORE), ";
            $query .= "AVG(MOD_SCORE) ";
            $query .= "FROM TESTSCORE_DAT ";
            $query .= "WHERE YEAR                   =   '" .$args["YEAR"] ."' ";
            $query .= "  AND SEMESTER               =   '" .$args["SEMESTER"] ."' ";
            $query .= "  AND TESTKINDCD             =   '" .$args["TESTKINDCD"] ."' ";
            $query .= "  AND TESTITEMCD             =   '" .$args["TESTITEMCD"] ."' ";
            $query .= "  AND CLASSCD                =   '" .$args["CLASSCD"] ."' ";
            $query .= "  AND SUBCLASSCD             =   '" .$args["SUBCLASSCD"] ."' ";
            $query .= "  AND ATTEND_FLG             =   '1' ";
            if($i == 1){
                $query .= "  AND ATTENDCLASSCD  IN  (" .$args["CurrentClassCode"] .") ";
            }else{
                $query .= "  AND ATTENDCLASSCD  IN  (" .$args["ClassCodeArray"] .")";
            }
            $row = $db->getRow($query);
            if (is_array($row)){
                if ($row[0]) $data["人数"][$i]       = $row[0];                //人数
                if ($row[1]) $data["標準偏差"][$i]   = round($row[1], 2);      //標準偏差
                if ($row[2]) $data["平均点"][$i]     = round($row[2], 2);      //平均点
            }
        }
        //偏差値、席次を求める
        $query = "SELECT ";
        $query .= "T1.SCHREGNO,";                                                               //0
        $query .= "T1.ATTENDCLASSCD,";                                                          //1
        $query .= "T1.MOD_SCORE, ";                                                              //2
        $query .= "Kojin_Hensa(T1.MOD_SCORE," .$data["標準偏差"][0] ."," .$data["平均点"][0] ."),";     //3
        $query .= "Kojin_Hensa(T1.MOD_SCORE," .$data["標準偏差"][1] ."," .$data["平均点"][1] .") ";     //4
        $query .= "FROM TESTSCORE_DAT  T1, ";
        $query .= "     SCHREG_REGD_DAT        T2 ";
        $query .= "WHERE T1.YEAR                    =   '" .$args["YEAR"] ."' ";
        $query .= "  AND T1.SEMESTER                =   '" .$args["SEMESTER"] ."' ";
        $query .= "  AND T1.TESTKINDCD              =   '" .$args["TESTKINDCD"] ."' ";
        $query .= "  AND T1.TESTITEMCD              =   '" .$args["TESTITEMCD"] ."' ";
        $query .= "  AND T1.CLASSCD                 =   '" .$args["CLASSCD"] ."' ";
        $query .= "  AND T1.SUBCLASSCD              =   '" .$args["SUBCLASSCD"] ."' ";
        $query .= "  AND T1.ATTEND_FLG              =   '1' ";
        $query .= "  AND T1.ATTENDCLASSCD  IN  (" .$args["ClassCodeArray"] .") ";
        $query .= "  AND T2.YEAR              =   T1.YEAR ";
        $query .= "  AND T2.SCHREGNO          =   T1.SCHREGNO ";
        $query .= "ORDER BY T1.MOD_SCORE DESC, T2.GRADE || '-' || T2.HR_CLASS  || '-' || T2.ATTENDNO ";

        $result = $db->query($query);
        $array = explode(",", $args["CurrentClassCode"]);
        while( $row = $result->fetchRow()){
            //校内席次
            if ($score1 <> $row[2]){
                $rank1 += $TieScore1 + 1;
                $score1 = $row[2];
                $TieScore1 = 0;
            }else{
                $TieScore1++;
            }
            if (in_array("'" .$row[1] ."'", $array)){
                $args2[$row[0]]["Sekiji1"]    =  $rank1;
                $args2[$row[0]]["Hensa1"]     =  $row[3];
            }
            //学級席次
            if ($row[1] == $args["ATTENDCLASSCD"]){
                if ($score0 != $row[2]){
                    $rank0 += $TieScore0 + 1;
                    $score0 = $row[2];
                    $TieScore0 = 0;
                }else{
                    $TieScore0++;
                }
                $args2[$row[0]]["Sekiji2"]    =  $rank0;
                $args2[$row[0]]["Hensa2"]     =  $row[4];
            }

        }

        Query::dbCheckIn($db);
        return true;
    }
    /*

        @(f)

        機能      : アクセスログ登録関数

        返り値    :

        引き数    : ARG-1 : アクセス区分
                    ログイン：L
                    ログオフ：O
                    追加    ：I
                    更新    ：U
                    削除    ：D
                    実行    ：E
                    出力(プレビュー)：P

                    ARG-2 : 失敗区分
                    成功    ：0
                    失敗    ：1

        機能説明  : アクセスログ登録

        備考      :

    */
    function access_log($access_cd, $succcess_cd=0, $programid = "", $uid = "")
    {
        global $auth;

        if (defined("PROGRAMID") && $programid == ""){ // 引用符で括られている必要があります
            $programid = PROGRAMID;
        }
        //アクセス区分
        $arg = array("login"    => "L",     //ログイン
                     "logout"   => "O",     //ログオフ
                     "add"      => "I",     //追加
                     "update"   => "U",     //更新
                     "delete"   => "D",     //削除
                     "execute"   => "E",    //実行
                     "preview"   => "P",    //出力（プレビュー）
                     "start"    => "S",     //開始
                     "finish"   => "F"      //終了
                );

        if (isset($arg[$access_cd])){
            $REMOTE_IDENT   = getenv("REMOTE_INDENT");
            $REMOTE_ADDR    = getenv("REMOTE_ADDR");

            //一旦DBを閉じる
            if ($succcess_cd == 1){
                $db = Query::dbCheckOut();
                Query::dbCheckIn($db);
            }
            $userid = strlen($uid) ? $uid : $auth->auth["uid"];
            if ($userid != "" && $arg[$access_cd] != "" && is_numeric($succcess_cd)) {
                $db = Query::dbCheckOut();

                $data["UPDATED"][NUMBER]        = "sysdate()";
                $data["USERID"][TEXT]           = $userid;
                $data["PROGRAMID"][TEXT]        = $programid;
                $data["PCNAME"][TEXT]           = $REMOTE_IDENT;
                $data["IPADDRESS"][TEXT]        = $REMOTE_ADDR;
                $data["access_cd"][TEXT]        = $arg[$access_cd];
                $data["success_cd"][NUMBER]     = $succcess_cd;

                $query = Query::insertSQL($data, "ACCESS_LOG");

                $db->query($query);
                Query::dbCheckIn($db);
            }
        }
        return;

    }
    /*

        @(f)

        機能      : アクセスログDETAIL登録関数

        返り値    :

        引き数    : ARG-1 : アクセス区分
                    追加    ：I
                    更新    ：U
                    削除    ：D
                    実行    ：E
                    出力(プレビュー)：P

                    ARG-2 : 失敗区分
                    成功    ：0
                    失敗    ：1

        機能説明  : アクセスログDETAIL登録

        備考      :

    */
    function access_log_detail($access_cd, $postData, $getData, $programid)
    {
        global $auth;

        if (defined("PROGRAMID") && $programid == ""){ // 引用符で括られている必要があります
            $programid = PROGRAMID;
        }

        $REMOTE_IDENT   = getenv("REMOTE_INDENT");
        $REMOTE_ADDR    = getenv("REMOTE_ADDR");

        $setpostData = "";
        $sep = "";
        $postData = is_array($postData) ? $postData : array();
        foreach ($postData as $key => $val) {
            if (is_array($val)) {
                $setpostData .= $sep.$key . " = ". implode("/", $val);
            } else {
                $setpostData .= $sep.$key . " = ". $val;
            }
            $sep = "#";
        }

        $setgetData = "";
        $sep = "";
        $getData = is_array($getData) ? $getData : array();
        foreach ($getData as $key => $val) {
            if (is_array($val)) {
                $setgetData .= $sep.$key . " = ". implode("/", $val);
            } else {
                $setgetData .= $sep.$key . " = ". $val;
            }
            $sep = "#";
        }

        if ($auth->auth["uid"] != "" && $access_cd != "") {
            $db = Query::dbCheckOut();

            $data["UPDATED"][NUMBER]        = "sysdate()";
            $data["USERID"][TEXT]           = $auth->auth["uid"];
            $data["STAFFCD"][TEXT]          = STAFFCD;
            $data["PROGRAMID"][TEXT]        = $programid;
            $data["PCNAME"][TEXT]           = $REMOTE_IDENT;
            $data["IPADDRESS"][TEXT]        = $REMOTE_ADDR;
            $data["ACCESS_CD"][TEXT]        = $access_cd;
            $data["SUCCESS_CD"][NUMBER]     = 0;
            $data["POST_DATA"][TEXT]        = $setpostData;
            $data["GET_DATA"][TEXT]         = $setgetData;

            $query = Query::insertSQL($data, "ACCESS_LOG_DETAIL");

            $db->query($query);
            Query::dbCheckIn($db);
        }
        return;

    }
     //////////////////////////////////////
    ///.    csv から配列へ変換
    function    csv2array( $str, $sep=',' )
    {
        if( !is_string( $str ) )    return    false;

        ///    差し支えない制御符号を用意
        $DQUATE = "\x01";
        $COMMA = "\x02";

        ///    ２つの連続する引用符を別符号へ変換
        $str = str_replace( '""', $DQUATE, $str );
        ///    " ", または " "\n に挟まれた区間の改行とカンマ（セパレータ）を別記号へ変換
        $pattern = '"(.+?)"'.$sep.'|"(.+?)"';
        mbereg_search_init( $str, $pattern );
        while( true )
        {
            $sp = mbereg_search_getpos();                ///    開始位置の記録
            $tp = mbereg_search_pos( $pattern, 'p' );    ///    みつかった位置
            if( is_array( $tp ) )
            {
                $p = $tp[0];                            ///    一致先頭
                $s = $tp[1];                            ///    一致サイズ
                $prev =  (int)$p - (int)$sp;                        ///    読み飛ばし文字サイズ
                $newstr .= substr( $str, $sp, $prev );    ///    読み飛ばし文字列をコピー
                $l = mbereg_search_getregs();                ///    検索パターンの取り出し
                if($l[1] != "" ){
                    ///    置き換え文字に差し替え
                    $replace = $l[1];
                    $replace = str_replace( $sep, $COMMA, $replace ).$sep;
                }else{
                    ///    置き換え文字に差し替え
                    $replace = $l[2];
                    $replace = str_replace( $sep, $COMMA, $replace );
                }
                $newstr .= $replace;
                $sp = (int)$p + (int)$s;    ///    次の探索開始
                if( false == mbereg_search_setpos( $sp ) )    break;
            }
            else    break;
        }
        $total = strlen( $str );
        $remain = $total - (int)$sp;
        if( !empty( $remain ) )    $newstr .= substr( $str, $sp, $remain );
        ///    配列へ分解
        $lines = explode($sep, $newstr);
        foreach($lines as $key => $val){
            ///    変換した記号を元の記号に戻す
            $val = str_replace( $COMMA, $sep, $val );
            if ($val == $DQUATE){
                $val = "";
            }else{
                $val = str_replace( $DQUATE, '"', $val );
            }
            $lines[$key] = $val;
        }

        return    $lines;
    }
    //ファイルダウンロード
    function downloadContents($contents, $filename, $encode="SJIS-win"){
        $contents = mb_convert_encoding($contents, $encode, mb_internal_encoding());

        /* HTTPヘッダの出力 */
        mb_http_output("pass");
        header("Accept-Ranges: none");
        if (stristr($_SERVER['HTTP_USER_AGENT'], "MSIE") || stristr($_SERVER['HTTP_USER_AGENT'], "Trident")) {
            // IEの場合
            $filename = mb_convert_encoding($filename,"SJIS-win");
            header("Content-Disposition: inline; filename=$filename");
        } else {
            header("Content-disposition: attachment; filename*=UTF-8''".rawurlencode($filename));
        }
        header("Content-Transfer-Encoding: binary");
        header("Content-Length: ". strlen($contents) );
        header("Content-Type: text/octet-stream");
//        header("Content-Type: application/octet-stream");

        echo $contents;
    }
    //ダウンロード用のHTTPヘッダ送信
    function setDownloadHeader($filename){
        if (stristr($_SERVER['HTTP_USER_AGENT'], "MSIE") || stristr($_SERVER['HTTP_USER_AGENT'], "Trident")) {
            // IEの場合
            $filename = mb_convert_encoding($filename,"SJIS-win");
            header("Content-Disposition: attachment; filename=\"".basename($filename)."\"");
        } else {
            header("Content-disposition: attachment; filename*=UTF-8''".rawurlencode($filename));
        }
    }
    /*
        機能      : 年組取得

        返り値    : $query:クエリー文

        引き数    : $year     :年度
                    $semester :学期
                    $auth_user:権限(ユーザー)
                    $staffcd  :職員コード(ユーザー)

        機能説明  : 年組取得（権限）クエリー文

        備考      :
     */
    function getHrClassAuth($year,$semester,$auth_user,$staffcd)
    {
        //参照・更新可
        if ($auth_user == DEF_REFERABLE || $auth_user == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .$staffcd ."' ";
            $query .= " OR TR_CD2 = '" .$staffcd ."' ";
            $query .= " OR TR_CD3 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD3 = '" .$staffcd ."') ";
        }

        return $query;
    }

    function getMaxByteStr($strAll, $maxByte, $hankakuKanaSize = 2) {

        $hankakuKana = array("ｱ", "ｲ", "ｳ", "ｴ", "ｵ",
                             "ｶ", "ｷ", "ｸ", "ｹ", "ｺ",
                             "ｻ", "ｼ", "ｽ", "ｾ", "ｿ",
                             "ﾀ", "ﾁ", "ﾂ", "ﾃ", "ﾄ",
                             "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ",
                             "ﾊ", "ﾋ", "ﾌ", "ﾍ", "ﾎ",
                             "ﾏ", "ﾐ", "ﾑ", "ﾒ", "ﾓ",
                             "ﾔ", "ﾕ", "ﾖ",
                             "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ",
                             "ﾜ", "ｦ", "ﾝ",
                             "ｧ", "ｨ", "ｩ", "ｪ", "ｫ",
                             "ｬ", "ｭ", "ｮ", "ｯ", "ｰ",
                             "ﾞ", "ﾟ", "｡", "｢", "｣", "､", "･");

        $cnt = 0;
        $retStr = "";
        for ($i = 0; $i < mb_strlen($strAll, "UTF-8"); $i++) {
            $getChar = mb_substr($strAll , $i, 1, "UTF-8");
            $getCharByte = strlen(bin2hex($getChar)) / 2;
            if (in_array($getChar, $hankakuKana)) {
                $getCharByte = (int)$hankakuKanaSize;
            } else {
                $getCharByte = $getCharByte == 1 ? 1 : 2;
            }
            $cnt += $getCharByte;
            if ($maxByte < $cnt) {
                break;
            }
            $retStr = $retStr.$getChar;
        }
        return $retStr;
    }

    /*
        機能      : パスワードの暗号化変換。
        返り値    : 暗号化パスワード。
                  : エラー時はnullが帰ります。
        引き数    : $_passwd - 暗号化前のパスワード。
                  : $_randm  - ランダム文字列。'0'～'9','a'～'f'('A'～'F')の文字。
                  : $_url    - URL埋込み用の変換をします。
        機能説明  : パスワードはランダム文字列と組み合わさって符号化されます。
                  : ワンタイムパスワードとなり、盗聴されても使用不可になります。
        備考      : 16ビットバイナリー構成
                  : 0123456789ABCDEF　←ビット番号
                  : R*PRPRPRPRPRPRPR　←R=ランダム文字列、*=乱数ビット、P=パスワード。
                  : 16ビットバイナリーに１文字のパスワードとランダム文字が組込まれます。
                  : パスワードが終わるまで繰り返します。パスワードはASCII文字です。
                  : パスワードとランダム文字列の長さは１文字以上です。
                  : 結果はBase64エンコードされます。
        2017/07/06 ランダム文字列の長さ制限を解除。URL埋込み用変換を追加。
    */
    function passwdEncode($_passwd,$_randm,$_url=false) {
        //パラメータチェック
        $len = strlen($_passwd);
        $rlen = strlen($_randm);
        if (($len<=0)||($rlen<=0)) {
            return null;
        }
        $__randm = $_randm;
        if (($rlen%2) != 0) {
            $__randm.="0"; $rlen++;
        }
        //パスワードの暗号化変換
        $password_asc = "";
        for ($i = 0; $i < $len; $i++) {
            $c=substr($_passwd, $i, 1);
            $ch=ord($c);
            if (rand(0,1)>0)
                $ch|=128;
            $ch2=substr($__randm, ($i*2) % $rlen, 2);
            $r2=hexdec($ch2);
            $r0=0; $n=1; $b=1;
            for($l=0;$l<8;$l++) {
                if (($ch&$n)!=0)
                    $r0|=$b;
                $b<<=1;
                if (($r2&$n)!=0)
                    $r0|=$b;
                $b<<=1;
                $n<<=1;
            }
            //暗号化したデータを16進で蓄積
            $ch0=base_convert($r0,10,16);
            $ln0=strlen($ch0);
            if ($ln0<4)
                $ch0=substr("0000",0,4-$ln0).$ch0;
            else if ($ln0>4)
                $ch0=substr($ch0,$ln0-4,4);
            $password_asc.=$ch0;
        }
        //16進で蓄積データをバイナリーに変換
        $password_bin=pack("H*",$password_asc);
        //バイナリーを文字列に変換
        $password_base64 = base64_encode($password_bin);
        if ($_url==false)
            return $password_base64;
        //URL埋込み用変換
        $search  = array('+', '/', '=');
        $replace = array('_', '-', '.');
        return str_replace($search, $replace, $password_base64);
    }

    /*
        機能      : パスワードの暗号化解読。
        返り値    : 解読したパスワード。
                  : エラー時はnullが帰ります。
        引き数    : $_input  - Base64エンコードされた暗号化パスワード。
                  : $_randm  - ランダム文字列。
                  : $_url    - URL埋込み用の変換をします。
        機能説明  : パスワードはランダム文字列と組み合わさって符号化されます。
                  : ワンタイムパスワードとなり、盗聴されても使用不可になります。
        備考      : 関数内でBase64デコードして処理します。
                  : 16ビットバイナリー構成
                  : 0123456789ABCDEF　←ビット番号
                  : R*PRPRPRPRPRPRPR　←R=ランダム文字列、*=無視、P=パスワード。
                  : 16ビットバイナリーで１文字のパスワードとランダム文字が取り出せます。
                  : 暗号化パスワードが終わるまで繰り返します。
                  : ランダム文字列をパスワードの長さ分取出します。
                  : 取出したランダム文字と引数のランダム文字を比較し不一致ならエラーにします。
        2017/07/06 ランダム文字列の長さ制限を解除。URL埋込み用変換を追加。
    */
    function passwdDecode($_input, $_randm,$_url=false) {
        //暗号化パスワードをバイナリーに変換
        $passwd_bin="";
        if ($_url==false) {
            $passwd_bin.=base64_decode($_input);
        }
        else {  //URL埋込み用変換
            $search  = array('_', '-', '.');
            $replace = array('+', '/', '=');
            $input_base64 = str_replace($search, $replace, $_input);
            $passwd_bin.=base64_decode($input_base64);
        }
        $passwd_asc = bin2hex($passwd_bin);
        $len = strlen($passwd_asc)/4;
        $rlen = strlen($_randm);
        //パラメータチェック
        if (($len<=0)||($rlen<=0)) {
            return null;
        }
        //パスワードの暗号化解読
        $passwd0="";
        $__randm = $_randm;
        if (($rlen%2) != 0) {
            $__randm.="0"; $rlen++;
        }
        for ($i = 0; $i < $len; $i++) {
            $ch2=substr($passwd_asc, $i*4, 4);
            $r2=hexdec($ch2);
            $r0=0; $p0=0; $n=1; $b=1;
            for($l=0;$l<8;$l++) {
                if (($r2&$n)!=0)
                    $p0|=$b;
                $n<<=1;
                if (($r2&$n)!=0)
                    $r0|=$b;
                $b<<=1;
                $n<<=1;
            }
            $p0&=127;
            $passwd0.=chr($p0);
            $hex=base_convert($r0,10,16);
            $ln0=strlen($hex);
            if ($ln0<2)
                $hex=substr("00",0,2-$ln0).$hex;
            else if ($ln0>2)
                $hex=substr($hex,$ln0-2,2);
            //ランダム文字列と比較
            $abs = substr($__randm, ($i*2) % $rlen, 2);
            if (strcmp($hex, $abs) != 0)
                return null;
        }
        return $passwd0;
    }


//-----------------------------------------------------------------------------------------------

    /*2009-08-26追加
        機能      : 日付に対して配列を返す
        返り値    : $nd 配列
        引き数    : $d  日付(20XX-01-01, 20XX0101, HXX/01/01の３通り。短くても良い) 
        機能説明  : タイムスタンプ、年月日、漢字年号、本日との比較結果、などを返す
        備考      : Mycalendarを利用
        追加      : 2012/10/25 2038年問題対策でdateコマンドとstrtotimeコマンドを変更した。      

    */

function dateconv3($d=""){

    if(trim($d)==""){
        return;
    }
    $myq = new mycalendar();
    $nd["input"] = $d;
    //和暦のリストを取得
    $warekiList = common::getWarekiList();

    //２０１０年５月１９日金丸追加↓
    //新日付を変換（4220519）を（H22/05/19）へ　とりあえず７桁文字なしを該当するとみなす。
    //同様に（4）や（422）や（42205）の１桁、３桁、５桁文字なしも同様にみなす。
    $num = "/[0-9]/s";
    //年号のみ
    if(strlen($d) == 1 && preg_match($num,$d)){
        foreach ($warekiList as $value) {
            if ($value["CD"] == substr($d,0,1)){
                $d = $value["SName"];
                break;
            }
        }
    }
    //年のみ
    if(strlen($d) == 3 && preg_match($num,$d)){
        foreach ($warekiList as $value) {
            if ($value["CD"] == substr($d,0,1)){
                $d = $value["SName"].substr($d,1,2);
                break;
            }
        }
    }
    //年月のみ
    if(strlen($d) == 5 && preg_match($num,$d)){
        foreach ($warekiList as $value) {
            if ($value["CD"] == substr($d,0,1)){
                $d = $value["SName"].substr($d,1,2)."/".substr($d,3,2);
                break;
            }
        }
    }
    if(strlen($d) == 7 && preg_match($num,substr($d,4,1))){
        foreach ($warekiList as $value) {
            if ($value["CD"] == substr($d,0,1)){
                $d = $value["SName"].substr($d,1,2)."/".substr($d,3,2)."/".substr($d,5,2);
                break;
            }
        }
    }
    //２０１０年５月１９日ここまで↑

    //入力タイプの判別
    $num = "/[0-9]/s";
    if(!preg_match($num,substr($d,0,1))){ 
        $kind = 3;  //和暦
    }else{
        if(!preg_match($num,substr($d,4,1))){        
            $kind = 1;  //20XX-01-01
        }else{
            $kind = 2;  //20XX0101
        }
    }

    //長さの判別 ： 計算に必要な部分を補い、20XX-01-01型に変換
    switch($kind){
        case(1):
            switch(strlen($d)){
                case(4):
                    $d = $d."-12-31";  //年号の境界では新しいほう優先
                    $type = "y";
                    break;
                case(7):
                    $d = $d."-01";
                    $type = "ym";
                    break;
                case(10):
                    $type = "ymd";
                    break;
                default:
                    return $d;
            }
            break;
        case(2):
            switch(strlen($d)){
                case(4):
                    $d = $d."-12-31";   //年号の境界では新しいほう優先
                    $type = "y";
                    break;
                case(6):
                    $d = substr($d,0,4)."-".substr($d,4,2)."-01";
                    $type = "ym";
                    break;
                case(8):
                    $d = substr($d,0,4)."-".substr($d,4,2)."-".substr($d,6,2);
                    $type = "ymd";
                    break;
                default:
                    return $d;
            }
            break;        
        case(3):
            switch(strlen($d)){
                case(1):    //"H"→年号だけ変換して返す
                    foreach ($warekiList as $value) {
                        if ($value["SName"] == substr($d,0,1)){
                            $d = str_replace($value["SName"], $value["Name"], $d);
                            break;
                        }
                    }
                    $nd["nengo"] = $d;
                    return($nd);
                case(3):
                    if($d=="S64"||$d=="T15"||$d=="M45"){    //境界では入力に従う  <-- 20120529 安田 修正（明治の境界ミス）
                        $d = $d."/01/01";   //古いほうの年号が出るように
                    }else{
                        $d = $d."/12/31";   //新しいほうの年号が出るように
                    }
                    $type = "y";
                    break;
                case(6):
                    $d = $d."/01";   
                    $type = "ym";
                    break;
                case(9):
                    $type = "ymd";
                    break;
                default:
                    return $d;
            }
            $d = $myq->ChgJToW($d);
            break;
        default:
            return $nd["input"];
    }

    //1901年以前はエラー <- 20120529 修正（1901年の前半はタイムスタンプが空になり、処理異常をきたす）
    if(substr($d,0,4)<=1901){
        return $nd["input"];
    }

    //西暦→タイムスタンプ(以降の処理の基幹)
    $t = new DateTime($d);//20121025　2038対策 - date_create($d)と同じ意味
    
    //西暦年、月、日、西暦年月日
    $wy   = $t->format("Y");    //20121025　2038対策 - date_format($t,"Y")と同じ意味
    $m    = $t->format("m");    //20121025　2038対策 - date_format($t,"m")と同じ意味
    $d    = $t->format("d");    //20121025　2038対策 - date_format($t,"d")と同じ意味
    $wym  = $t->format("Y-m");  //20121025　2038対策 - date_format($t,"Y-m")と同じ意味
    $wymd = $t->format("Y-m-d");//20121025　2038対策 - date_format($t,"Y-m-d")と同じ意味

    if(substr($m,0,1)=="0"){
        $m_short = " ".substr($m,1,1);
    }else{
        $m_short = $m;
    }
    if(substr($d,0,1)=="0"){
        $d_short = " ".substr($d,1,1);
    }else{
        $d_short = $d;
    }

    //西暦年度
    if($m<=3){
        $wnend = $wy-1;
    }else{
        $wnend = $wy;
    }

    //月分
    $tbun   = $wy.$m;  

    //月日分
    $tdbun   = $wy.$m.$d;  

    //月日分
    $tabun   = $m >= 4 ? $wy."04"."01" : ($wy-1)."04"."01";  

    //西暦月末年月日
    if($m =="04"||$m =="06"||$m =="09"||$m =="11"){
        $matu = "30";
    }else{
        $matu = "31";
    }
    if($m =="02"){  //閏年日付は、３月１日から１日引いて求める
        $endday = new DateTime($wy."-03-01");//20121025　2038対策 //もしくはその月の末日ということで下でもいい
        $endday->modify("-1 days");         //20121025　2038対策 //$endday= new DateTime($wy."02-01");
        $matu   = $endday->format("d");     //20121025　2038対策 //$endday->format('t');//tが末日を示す
    } 
    $wgmatu = $wy."-".$m."-".$matu;

    //本日との比較結果
    $today = new DateTime();//20121025　2038対策 -現在日（タイムスタンプではないがタイムスタンプっぽい形で比較が可能)
    switch(true){
      case $t > $today : $comp = 1 ; break;//20121025　2038対策 -現在日
      case $t== $today : $comp = 0 ; break;//20121025　2038対策 -現在日
      case $t < $today : $comp = -1; break;//20121025　2038対策 -現在日
      default          : $comp ="" ;       //20121025　2038対策 -現在日
     }

    //和暦
    $jymd = $myq->ChgWToJ($wymd);
    $jy   = substr($jymd,0,3);
    $jy_2 = substr($jymd,1,2);
    $jym  = substr($jymd,0,6);
    //年度
    $jnend  = substr($myq->ChgWToJ($wnend."-04-01"),0,3);
    //和月末
    $jgmatu = $myq->ChgWToJ($wgmatu);
    //年号全角
    foreach ($warekiList as $value) {
        if ($value["SName"] == substr($jy,0,1)){
            $nengo1 = $value["Name"];
            break;
        }
    }
    //年月日
    $jy2 = substr($jy,1,2);
    $m2  = $m;
    $d2  = $d;
    //20110523 add
    if(substr($jy2,0,1)=="0"){
        $jy2_short = " ".substr($jy2,1,1);
    }else{
        $jy2_short = $jy2;
    }
    
    $jymd2 = $jy.".".$m_short.".".$d_short; //20110523 add
    $jym2  = $jy.".".$m_short;              //20110528 add
    $jymd3 = $jy."/".$m_short."/".$d_short; //20110613 add
    $jym3  = $jy."/".$m_short;              //20110613 add
    
    settype($jy2, "integer");
    settype($m2,  "integer");
    settype($d2,  "integer");
    $jy2 = mb_convert_kana($jy2,"A");
    $m2  = mb_convert_kana($m2,"A");
    $d2  = mb_convert_kana($d2,"A");
    if($jy2=="１"){
       $jy2 = str_replace("１","元",$jy2);
    }
    $ymdzen = $nengo1.$jy2."年".$m2."月".$d2."日";
    $ymdhan = $nengo1.substr($jy,1,2)."年".$m."月".$d."日";
    //$ymdhan2 = $nengo1.substr($jy,1,2)."年".$m_short."月".$d_short."日";20110523 del
    $ymdhan2 = $nengo1.$jy2_short."年".$m_short."月".$d_short."日";//20110523 add
    $ymzen = $nengo1.$jy2."年".$m2."月";
    $ymhan = $nengo1.substr($jy,1,2)."年".$m."月";
    $ymhan2 = $nengo1.substr($jy,1,2)."年".$m_short."月";
    $yzen = $nengo1.$jy2."年";
    $yhan = $nengo1.substr($jy,1,2)."年";

    //月分
    $tbunzen = $nengo1.$jy2."年".$m2."月分";
    $tbunhan = $nengo1.substr($jy,1,2)."年".$m."月分";
    $tbunhan2 = $nengo1.substr($jy,1,2)."年".$m_short."月分";

    //曜日
    //$res = date("w", $t);
    $res = $t->format('w');//20121025　2038対策
    $day = array("日", "月", "火", "水", "木", "金", "土");
    $youbi = $day[$res];

    //年度
    foreach ($warekiList as $value) {
        if ($value["SName"] == substr($jnend,0,1)){
            $nengo2 = $value["Name"];
            break;
        }
    }
    $jnend2 = substr($jnend,1,2);
    settype($jnend2, "integer");
    $jnend2 = mb_convert_kana($jnend2,"A");
    if($jnend2=="１"){
       $jnend2 = str_replace("１","元",$jnend2);
    }
    $nendzen = $nengo2.$jnend2."年度";
    $nendhan = $nengo2.substr($jnend,1,2)."年度";

    //月末
    foreach ($warekiList as $value) {
        if ($value["SName"] == substr($jgmatu,0,1)){
            $nengo3 = $value["Name"];
            break;
        }
    }
    $jmatunen2 = substr($jgmatu,1,2);
    $matu2 = $matu;
    settype($jmatunen2, "integer");
    settype($matu2, "integer");
    $jmatunen2 = mb_convert_kana($jmatunen2,"A");
    $matu2 = mb_convert_kana($matu2,"A");
    if($jmatunen2=="１"){
       $jmatunen2 = str_replace("１","元",$jmatunen2);
    }
    $gmatuzen = $nengo3.$jmatunen2."年".$m2."月".$matu2."日";
    $gmatuhan = $nengo3.substr($jgmatu,1,2)."年".$m."月".$matu."日";
    $gmatuhan2 = $nengo3.substr($jgmatu,1,2)."年".$m_short."月".$matu."日";

    //GYYMMDD
    foreach ($warekiList as $value) {
        if ($value["SName"] == substr($jy,0,1)){
            $nengo3 = $value["CD"];
            break;
        }
    }
    $gy = $g.substr($jy,1,2);
    $gym = $g.substr($jy,1,2).$m;
    $gymd = $g.substr($jy,1,2).$m.$d;


    //入力が短い場合、不確かなものは出力しない
    if($type != "ymd"){
        $t =""; $d=""; $wymd=""; $jymd=""; $gymd="";
        $ymdzen=""; $ymdhan=""; $ymdhan2=""; $comp=""; $youbi="";
        
        if($type != "ym"){
            $m =""; $wym=""; $jym=""; $gym=""; $ymzen=""; $ymhan=""; $ymhan2="";
            $wnend=""; $wgmatu=""; $tbun=""; $jnend="";
            $jgmatu=""; $nendzen=""; $gmatuzen=""; $tbunzen="";
            $nendhan=""; $gmatuhan=""; $tbunhan=""; $gmatuhan2=""; $tbunhan2=""; 
        }
    }

    //配列ndを出力
        //$nd["input"] に引数が入っている           例：2008-02-01を入力した場合                                     
    $nd["time"]     = $t;        //タイムスタンプ   //1201791600
    $nd["wy"]       = $wy;       //西暦年           //2008
    $nd["m"]        = $m;        //月               //02
    $nd["d"]        = $d;        //日               //01
    $nd["msh"]      = $m_short;  //月               //2
    $nd["dsh"]      = $d_short;  //日               //1
    $nd["wym"]      = $wym;      //西暦年月         //2008-02
    $nd["wymd"]     = $wymd;     //西暦年月日       //2008-02-01
    $nd["wnend"]    = $wnend;    //西暦年度         //2007
    $nd["wgmatu"]   = $wgmatu;   //西暦月末日       //2008-02-29
    $nd["tbun"]     = $tbun;     //月分（6ケタ）    //200802
    $nd["tdbun"]    = $tdbun;    //月日分（8ケタ）  //20080201
    $nd["tabun"]    = $tabun;    //月日分（8ケタ）  //20080401(必ず該当年度の４月１日が返る）
    $nd["jy"]       = $jy;       //和暦年           //H20
    $nd["jy2"]      = $jy_2;     //和暦年(数字のみ) //20
    $nd["jym"]      = $jym;      //和暦年月         //H20/02
    $nd["jymd"]     = $jymd;     //和暦年月日       //H20/02/01
    $nd["jymd2"]    = $jymd2;    //和暦年月日       //H20.02.01
    $nd["jymd3"]    = $jymd3;    //和暦年月日       //H20/ 2/ 1
    $nd["jym2"]     = $jym2;     //和暦年月         //H20.02
    $nd["jym3"]     = $jym3;     //和暦年月         //H20/ 2
    $nd["jnend"]    = $jnend;    //和暦年度         //H19
    $nd["jgmatu"]   = $jgmatu;   //和暦月末         //H20/02/29
    $nd["gy"]       = $gy;       //GYYMM表記        //420
    $nd["gym"]      = $gym;      //GYYMM表記        //42002
    $nd["gymd"]     = $gymd;     //GYYMMDD表記      //4200201

    $nd["youbi"]    = $youbi;    //全角曜日         //金
    $nd["nengo"]    = $nengo1;   //全角年号         //平成
    $nd["yzen"]     = $yzen;     //全角年           //平成２０年   
    $nd["yhan"]     = $yhan;     //半角年           //平成20年   
    $nd["ymzen"]    = $ymzen;    //全角年月         //平成２０年２月   
    $nd["ymhan"]    = $ymhan;    //半角年月         //平成20年02月   
    $nd["ymhan2"]   = $ymhan2;   //半角年月2        //平成20年2月   

    $nd["ymdzen"]   = $ymdzen;   //全角年月日       //平成２０年２月１日   
    $nd["ymdhan"]   = $ymdhan;   //半角年月日       //平成20年02月01日
    $nd["ymdhan2"]  = $ymdhan2;  //半角年月日2      //平成20年2月01日

    $nd["nendzen"]  = $nendzen;  //全角年度         //平成１９年度
    $nd["nendhan"]  = $nendhan;  //半角年度         //平成19年度
    $nd["gmatuzen"] = $gmatuzen; //全角月末日       //平成２０年２月２９日
    $nd["gmatuhan"] = $gmatuhan; //半角月末日       //平成20年02月29日
    $nd["gmatuhan2"] = $gmatuhan2; //半角月末日2    //平成20年2月29日

    $nd["tbunzen"]  = $tbunzen;  //全角月分         //平成２０年２月分
    $nd["tbunhan"]  = $tbunhan;  //半角月分         //平成20年02月分
    $nd["tbunhan2"] = $tbunhan2; //半角月分         //平成20年2月分

    $nd["comp"]     = $comp;     //本日との比較（未来=1,当日=0,過去=-1） //-1

    //空箱整理
    foreach ($nd as $key => $val) {
        if($val==""){
            unset($nd[$key]);
        }
    }                              
    return $nd;
}

/*2012-08-14追加
//post変数から７ケタ整数で日付を受け取る
//※カレンダーの初期値は西暦に整形する必要がある。dateconv3関数で整形する。
*/
function getDateHenkan($value, $type) {
    $arr = common::dateconv3($value);
    return $arr[$type];
}


//2012-11-12 生保から移植
    /*
        機能      : 指定したフィールドの値をまとめて更新
        返り値    : ＤＢに入力したクエリ
        引き数    : $table - テーブル名 ,
                    $col   - 更新の条件に使う列名,     
                    $where - 更新の条件,         //$colと二者択一で、$whereを優先
                    $field - 更新データの入った配列 ,
                    $db    - $db = Query::dbCheckOut();
                    $mode  - 0-更新及び挿入, 1-更新のみ, 2-挿入のみ。初期値は2
        機能説明  : 配列の値をまとめて更新用queryにしてdbに適用する関数。
                    $this->field["UPDATETIME"] = "sysdate()";
                    $this->field["PRTNO"] = 20;
                    など、テーブルに名前を揃えた配列を渡す必要がある。
                    $modeが2の場合は挿入のみ行う（上書きトラブル防止）
        備考      : コミットまで一括して行う。
                    queryは、確認用に出力
    */
    function _update($table,$col="",$where="",$field,$db,$mode=2)
    {
        //更新モードで、whereと列名が両方無ければエラー
        if($mode==""){
            $mode=2;
        }
        if($where=="" && $col=="" && $mode!=2){
            $query = "function Update error<br>";
            return $query;
        }

        //列の型を取得。data配列に格納
        $query  = "SELECT NAME,TBNAME,TYPENAME  FROM SYSIBM.SYSCOLUMNS ";
        $query .= "WHERE TBNAME = '".$table."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $type = trim($row["TYPENAME"]);
            if($type=="DOUBLE" || $type=="INTEGER" || $type=="TIMESTAMP" ||
               $type=="TIME" || $type=="SMALLINT"){
                if($field[$row["NAME"]]!=""){
                    $data[$row["NAME"]][NUMBER] = $field[$row["NAME"]];
                }
                $flg = 1;
            }else{
                if($field[$row["NAME"]]!=""){
                    $data[$row["NAME"]][TEXT]   = $field[$row["NAME"]];
                }
                $flg = 0;
            }
            if($col==$row["NAME"]){ 
                $keytype = $flg;    //指定行が数字か文字か判別し、条件クエリ生成に利用
            }
        }

        //whereが無くcolが有り＝列名を判別して条件クエリを作成
        if($where=="" && $col !=""){
            if($keytype==1){
                $where = " WHERE ".$col." = ".$field[$col];
            }else{
                $where = " WHERE ".$col." = '".$field[$col]."' ";
            }
        }

        //mode 0 なら自動判別。1 なら更新。2 なら挿入。
        switch($mode){
            case(0):
                //DBにデータが存在すれば更新、なければ挿入
                $query  = " SELECT * FROM ".$table." ";
                $query .= $where;
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if(is_array($Row)){
                    $query = Query::updateSQL($data, $table, $where);
                }else{
                    $query = Query::insertSQL($data, $table);
                }
                break;
            case(1):
                $query = Query::updateSQL($data, $table, $where);
                break;
            case(2):
            default:
                $query = Query::insertSQL($data, $table);
        }
        $db->query($query);
        return $query;
    }

    /*
        CSV データ出力用、文字化け対策
        IBM-Unicode(標準Unicode)とMS-Unicodeが異なる文字を変換する
    
    */
    function getTokushuHenkan($s) {
        $retChar = '';

        //文字列を一文字ずつ配列に
        $strArray = common::mbStringToArray($s);
        foreach ($strArray as $key => $val) {
            //特殊なハイフンを全角ハイフンに変換
            if (preg_match('/\xE2\x88\x92/', $val) || preg_match('/\xE2\x80\x94/', $val)) {
                $retChar .= "‐";
            //特殊な中点を全角中点に変換
            } else if (preg_match('/\xE2\x80\x97/', $val)) {
                $retChar .= "・";
            //特殊な～を全角～に変換
            } else if (preg_match('/\xE3\x80\x9C/', $val)) {
                $retChar .= "～";
            //特殊な∥を全角∥に変換
            } else if (preg_match('/\xE2\x80\x96/', $val)) {
                $retChar .= "∥";
            } else {
                $retChar .= $val;
            }
        }
        return $retChar;
    }

    //文字列を一文字ずつ配列に
    function mbStringToArray($sStr, $sEnc='UTF-8') {
        $aRes = array();
        while ($iLen = mb_strlen($sStr, $sEnc)) {
            array_push($aRes, mb_substr($sStr, 0, 1, $sEnc));
            $sStr = mb_substr($sStr, 1, $iLen, $sEnc);
        }
        return $aRes;
    } 

    /*
        BackupテーブルINSERT処理 
    */
    function BackupRecord($tableArray, $recordArray, $programid, $setBackupDayPeriod="")
    {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $dataType = array();
        $dataType = array("CHARACTER VARYING" => "TEXT", 
                          "CHARACTER" => "TEXT", 
                          "DATE" => "TEXT", 
                          "DECIMAL" => "NUMBER",
                          "INTEGER" => "NUMBER", 
                          "SMALLINT" => "NUMBER", 
                          "TIME" => "TEXT", 
                          "TIMESTAMP" => "FUNC", 
                          );
                          
        //管理テーブルのMAXのDEL_SEQを取得
        $query  = "SELECT MAX(DEL_SEQ) FROM DELBK_KANRI_DAT WHERE DEL_DATE = '".date("Y-m-d")."' ";
        $maxDelSeq = $db->getOne($query);
        if (!$maxDelSeq) {
            $maxDelSeq = "1";
        } else {
            $maxDelSeq = (int)$maxDelSeq + 1;
        }
        
        $backup_table_name = "";
        $kaigyou = "";
        //Backupの期間をセット(デフォルトは7日間)
        if ($setBackupDayPeriod) {
            $oneWeekBefore = date("Y-m-d", strtotime("$setBackupDayPeriod day"));
        } else {
            $oneWeekBefore = date("Y-m-d", strtotime("-7 day"));
        }
        foreach ($tableArray as $tableName) {
        
            //フィールドタイプセット
            $fieldrow = array();
            $setDataType = array();
            $bkTableName = 'DELBK_'.$tableName;
            $query = "SELECT COLUMN_NAME, DATA_TYPE FROM SYSIBM.COLUMNS WHERE TABLE_NAME='{$bkTableName}' ORDER BY ORDINAL_POSITION ";
            $result = $db->query($query);
            while ($fieldrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setDataType[$fieldrow["COLUMN_NAME"]] = $dataType[$fieldrow["DATA_TYPE"]];
            }
            $result->free();

            //Backup対象テーブルで、削除実行日より1週間より古いデータをDelete
            $query = "DELETE FROM {$bkTableName} WHERE DEL_DATE < '".$oneWeekBefore."' ";
            $db->query($query);

            //Backup対象データを取得
            $query = "SELECT * FROM {$tableName} ";
            $query .= " WHERE ";
            $i = 0;
            foreach ($recordArray as $fieldName => $fielddata) {
                if ($i == 0) {
                    if ($setDataType[$fieldName] == "NUMBER") {
                        $query .= "     {$fieldName} = {$fielddata} ";
                    } else {
                        $query .= "     {$fieldName} = '{$fielddata}' ";
                    }
                } else {
                    if ($setDataType[$fieldName] == "NUMBER") {
                        $query .= " AND {$fieldName} = {$fielddata} ";
                    } else {
                        $query .= " AND {$fieldName} = '{$fielddata}' ";
                    }
                }
                $i++;
            }
            
            //対象データをBackupテーブルにINSERT
            $set_table = "";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["DEL_DATE"][TEXT] = date("Y-m-d");
                $data["DEL_SEQ"][NUMBER] = $maxDelSeq;
                foreach ($row as $fieldName => $val) {
                    if ($setDataType[$fieldName] == "NUMBER") {
                        $data[$fieldName][NUMBER] = $val;
                    } else {
                        $data[$fieldName][$setDataType[$fieldName]] = $val;
                    }
                }
                $data["DEL_REGISTERCD"][TEXT] = STAFFCD;
                $data["DEL_UPDATED"][FUNC] = "sysdate()";
                $query = Query::insertSQL($data, "{$bkTableName}");
                $db->query($query);
                //Backupテーブル名を取得(同一テーブルに複数データがある場合は、1つのみ表示)
                if ($bkTableName != $set_table) {
                    $backup_table_name .= $kaigyou.$bkTableName;
                    $kaigyou = "\n";
                }
                $set_table = $bkTableName;
            }
            $result->free();
        }
        
        //管理テーブルから削除実行日より1週間より古いデータをDelete
        $query = "DELETE FROM DELBK_KANRI_DAT WHERE DEL_DATE < '".$oneWeekBefore."' ";
        $db->query($query);

        //管理テーブルへINSERT処理
        $data = array();
        $data["DEL_DATE"][TEXT]     = date("Y-m-d");
        $data["DEL_SEQ"][NUMBER]    = $maxDelSeq;
        $data["USERID"][TEXT]       = STAFFCD;
        $data["PROGRAMID"][TEXT]    = $programid;
        $data["REMARK"][TEXT]       = $backup_table_name;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "DELBK_KANRI_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //日付の妥当性チェック
    public function isDate($datestr)
    {
        if ($datestr =="") {
            return true;
        }
        if (substr($datestr, 4, 1) == "") {
            return false;
        }
        $tmp = explode(substr($datestr, 4, 1), $datestr);
        if (!$tmp[1] || !$tmp[2] || !$tmp[0]) {
            return false;
        }
        if (!is_numeric($tmp[1]) || !is_numeric($tmp[2]) || !is_numeric($tmp[0])) {
            return false;
        }
        if (strlen($tmp[1]) > 2 || strlen($tmp[2]) > 2 || strlen($tmp[0]) > 4) {
            return false;
        }
        return checkdate($tmp[1], $tmp[2], $tmp[0]);
    }
}
?>
