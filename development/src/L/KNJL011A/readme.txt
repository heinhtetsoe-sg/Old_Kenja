// kanji=漢字
// $Id: dfbca6a216e820f4e127142c8a88ef23d5a95f99 $

2018/08/14  1.新規作成

2018/08/20  1.入学者の一覧画面(KNJL013A)からコールされた時の処理を追加

2018/08/30  1.特待区分名のフィールド名を変更

2018/11/26  1.文言修正。「試験ID」を「試験区分」に修正

2019/04/15  1.卒業年月の元号対応
            -- 初期値が固定で「4:平成」となっているのを修正

2019/11/13  1.志願者SEQを10桁に変更

2019/11/19  1.出身学校コードを直接入力し、カーソルが動いたら直ぐ、出身学校名が表示されるよう修正

2019/12/18  1.以下の修正をした。
            -- 検索を、 志願者SEQ → 受験番号に変更
            -- かな検索に受験番号を追加
            -- 英検見なし得点追加

2019/12/23  1.受験番号検索、かな検索機能の修正

2020/01/21  1.中学の場合、専併区分コンボは「専願」のみ選択可 → 併願も可

2020/04/13  1.志願者SEQ欄の横に新規ボタンを追加し、Hで始まる以下連番を取得。(H123456)
            -- 追加・削除ボタンは、Hで始まる志願者SEQのデータのみ可能
            -- 備考欄を追加
            -- 更新後、先頭の志願者が表示される不具合修正
            -- かな検索では、受験番号なしでもエラーとせず表示
            -- 文言”受験番号”をテキストの横に移動し、文言”検索項目”追加

2020/05/20  1.前回の修正
            -- 追加ボタンで登録する時に志願者SEQ(Hで始まる以下連番)を取得
            -- 追加処理中は応募情報の追加～終了ボタンは入力不可

2020/05/26  1.新規登録処理中にエラーをだしても、追加・更新ボタンなどの入力可・不可の状態が変わらないように修正

2020/10/14  1.以下の修正をした。
            -- 特待生の特待区分1、特待区分2、特待区分3を事前特待、特待申請、資格活用へ変更し、disabledへ変更
            -- 特待生のクラブ名、英検見なし得点をdisabledへ変更
            -- 特待生の仮決定を削除

2021/01/06  1.リファクタリング
            2.生年月日(和暦)を入力し、更新した際に生年月日(西暦)も同時に自動登録するよう修正

2021/01/07  1.卒業年月(和暦)を入力し、更新した際に卒業年度(西暦)も同時に自動登録するよう修正
            2.受験番号の入力欄を5桁から7桁に変更

2021/01/08  1.志願者SEQおよび受験者番号の英数字チェックを削除

2021/01/21  1.リファクタリング
            2.特待生情報は、入力不可

2021/01/21  1.下段の受験番号「-」区切りに対応。
