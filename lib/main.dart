import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Whiteboard')),
        body: const WhiteboardPage(),
      ),
    );
  }
}

class WhiteboardPage extends StatefulWidget {
  const WhiteboardPage({Key? key}) : super(key: key);

  @override
  State<WhiteboardPage> createState() => _WhiteboardPageState();
}

class _WhiteboardPageState extends State<WhiteboardPage> {
  late WhiteboardController _controller;
  bool _erase = false;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: Whiteboard(
            onWhiteboardCreated: (controller) {
              _controller = controller;
            },
          ),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () {
                setState(() {
                  _erase = false;
                  _controller.setMode('draw');
                });
              },
              child: const Text('Draw'),
            ),
            const SizedBox(width: 10),
            ElevatedButton(
              onPressed: () {
                setState(() {
                  _erase = true;
                  _controller.setMode('erase');
                });
              },
              child: const Text('Erase'),
            ),
          ],
        ),
      ],
    );
  }
}

class Whiteboard extends StatefulWidget {
  final void Function(WhiteboardController) onWhiteboardCreated;
  const Whiteboard({Key? key, required this.onWhiteboardCreated}) : super(key: key);

  @override
  State<Whiteboard> createState() => _WhiteboardState();
}

class _WhiteboardState extends State<Whiteboard> {
  static const String _viewType = 'whiteboard_view';
  MethodChannel? _channel;

  @override
  Widget build(BuildContext context) {
    return AndroidView(
      viewType: _viewType,
      onPlatformViewCreated: _onPlatformViewCreated,
      layoutDirection: TextDirection.ltr,
    );
  }

  void _onPlatformViewCreated(int id) {
    _channel = MethodChannel('whiteboard_$id');
    widget.onWhiteboardCreated(WhiteboardController(_channel!));
  }
}

class WhiteboardController {
  final MethodChannel _channel;
  WhiteboardController(this._channel);

  Future<void> setMode(String mode) async {
    await _channel.invokeMethod('setMode', {'mode': mode});
  }
}
